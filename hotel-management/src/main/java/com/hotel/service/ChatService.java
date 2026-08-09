package com.hotel.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.dto.ChatHotelResult;
import com.hotel.dto.ChatRequest;
import com.hotel.dto.ChatResponse;
import com.hotel.model.Hotel;
import com.hotel.model.Room;
import com.hotel.repository.HotelRepository;
import com.hotel.repository.RoomRepoRepository;

/**
 * Runs all hotel searches and availability checks locally. Gemini can only
 * phrase the returned safe data, so it cannot issue database operations.
 */
@Service
@Transactional(readOnly = true)
public class ChatService {
    private static final int MAX_RESULTS = 5;
    private static final List<String> SUGGESTIONS = List.of(
            "Show top-rated hotels", "Find hotels in Mumbai",
            "Are rooms available?", "Find budget rooms");
    private static final Pattern ISO_DATE = Pattern.compile("\\b(20\\d{2}-\\d{2}-\\d{2})\\b");
    private static final Pattern PRICE = Pattern.compile(
            "(?:under|below|less than|within|budget(?:\\s+of)?|₹|rs\\.?|inr)\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)(k)?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern RATING = Pattern.compile(
            "(?:rated|rating|above|over|at least|minimum)\\s*(\\d(?:\\.\\d)?)\\s*(?:\\+|stars?|star)?",
            Pattern.CASE_INSENSITIVE);

    private final HotelRepository hotelRepository;
    private final RoomRepoRepository roomRepository;
    private final BookingRoomService bookingRoomService;
    private final GeminiClient geminiClient;

    public ChatService(HotelRepository hotelRepository,
                       RoomRepoRepository roomRepository,
                       BookingRoomService bookingRoomService,
                       GeminiClient geminiClient) {
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.bookingRoomService = bookingRoomService;
        this.geminiClient = geminiClient;
    }

    public ChatResponse answer(ChatRequest request) {
        String message = request.getMessage().trim();
        List<Hotel> approved = hotelRepository.findByStatus(Hotel.Status.APPROVED);
        SearchQuery query = SearchQuery.from(message, approved);
        List<ChatHotelResult> hotels = query.catalogQuestion
                ? search(query, approved) : List.of();
        String fallback = fallback(query, hotels);
        String reply = geminiClient.generateReply(message, context(query, hotels, fallback))
                .orElse(fallback);
        return new ChatResponse(reply, hotels, SUGGESTIONS, geminiClient.isConfigured());
    }

    private List<ChatHotelResult> search(SearchQuery query, List<Hotel> approved) {
        List<ChatHotelResult> results = new ArrayList<>();
        for (Hotel hotel : approved) {
            if (!matches(query.city, hotel.getCity())
                    || !matches(query.state, hotel.getState())
                    || !matches(query.hotelName, hotel.getHotelName())) continue;

            List<Room> matching = roomRepository.findByHotel_HotelId(hotel.getHotelId())
                    .stream().filter(room -> matchesRoomType(query.roomType, room)).toList();
            if (query.roomType != null && matching.isEmpty() && !query.specificHotel) continue;

            List<Room> available = matching.stream()
                    .filter(room -> isAvailable(room, query)).toList();
            if ((query.requestedAvailability || query.hasDates())
                    && available.isEmpty() && !query.specificHotel) continue;

            BigDecimal lowest = matching.stream().map(Room::getPricePerNight)
                    .filter(price -> price != null).min(Comparator.naturalOrder()).orElse(null);
            if (query.maxPrice != null && (lowest == null || lowest.compareTo(query.maxPrice) > 0)) continue;

            double rating = hotel.getRating() == null ? 0.0 : hotel.getRating();
            if (query.minRating != null && rating < query.minRating) continue;
            results.add(new ChatHotelResult(hotel.getHotelId(), hotel.getHotelName(),
                    hotel.getCity(), hotel.getState(), rating, hotel.getDescription(), lowest,
                    matching.size(), available.size(), query.hasDates(),
                    query.checkIn == null ? null : query.checkIn.toString(),
                    query.checkOut == null ? null : query.checkOut.toString()));
        }

        Comparator<ChatHotelResult> byRating = Comparator.comparing(ChatHotelResult::getRating,
                Comparator.nullsLast(Comparator.reverseOrder()));
        Comparator<ChatHotelResult> byPrice = Comparator.comparing(ChatHotelResult::getLowestPrice,
                Comparator.nullsLast(Comparator.naturalOrder()));
        results.sort(query.sortByPrice ? byPrice.thenComparing(byRating)
                : byRating.thenComparing(byPrice));
        return results.stream().limit(MAX_RESULTS).toList();
    }

    private boolean isAvailable(Room room, SearchQuery query) {
        if (!Boolean.TRUE.equals(room.getAvailabilityStatus())) return false;
        if (!query.hasDates()) return true;
        return bookingRoomService.isRoomAvailable(room, query.checkIn, query.checkOut);
    }

    private boolean matchesRoomType(String requested, Room room) {
        return requested == null || (room.getRoomType() != null
                && room.getRoomType().toLowerCase(Locale.ROOT).contains(requested));
    }

    private boolean matches(String expected, String actual) {
        return expected == null || (actual != null && actual.equalsIgnoreCase(expected));
    }

    private String fallback(SearchQuery query, List<ChatHotelResult> hotels) {
        if (!query.catalogQuestion) {
            return "Hi! I can find approved hotels by city, compare ratings or prices, "
                    + "and check room availability. Try one of the questions below.";
        }
        if (hotels.isEmpty()) {
            String location = query.city != null ? " in " + query.city
                    : query.state != null ? " in " + query.state : "";
            return "I couldn't find an approved stay" + location
                    + " matching those filters. Try another city, a larger budget, or another room type.";
        }
        if (query.requestedAvailability || query.hasDates()) {
            String detail = query.hasDates() ? " for " + query.checkIn + " to " + query.checkOut
                    : " based on rooms currently enabled; share ISO check-in and check-out dates for an exact booking check";
            return "I found " + hotels.size() + " matching hotel"
                    + (hotels.size() == 1 ? "" : "s") + detail + ". Tap a hotel to view its rooms.";
        }
        return "I found " + hotels.size() + " matching approved hotel"
                + (hotels.size() == 1 ? "" : "s") + ". Results are ordered by "
                + (query.sortByPrice ? "lowest price" : "rating") + ". Tap a hotel to view its rooms.";
    }

    private String context(SearchQuery query, List<ChatHotelResult> hotels, String fallback) {
        StringBuilder value = new StringBuilder("Search: ").append(query.summary())
                .append("\nFallback: ").append(fallback).append("\nVerified results:\n");
        if (hotels.isEmpty()) return value.append("No matching approved hotels.").toString();
        for (ChatHotelResult hotel : hotels) {
            value.append("- ").append(hotel.getHotelName()).append(" | ")
                    .append(hotel.getCity()).append(", ").append(hotel.getState())
                    .append(" | rating ").append(hotel.getRating())
                    .append(" | from ").append(hotel.getLowestPrice() == null ? "not recorded" : "INR " + hotel.getLowestPrice())
                    .append(" | matching rooms ").append(hotel.getMatchingRoomCount())
                    .append(" | available rooms ").append(hotel.getAvailableRoomCount())
                    .append(Boolean.TRUE.equals(hotel.getAvailabilityChecked())
                            ? " (checked for supplied dates)" : " (inventory status only)")
                    .append('\n');
        }
        return value.toString();
    }

    private static final class SearchQuery {
        final boolean catalogQuestion;
        final boolean requestedAvailability;
        final boolean specificHotel;
        final String city;
        final String state;
        final String hotelName;
        final String roomType;
        final boolean sortByPrice;
        final BigDecimal maxPrice;
        final Double minRating;
        final LocalDate checkIn;
        final LocalDate checkOut;

        SearchQuery(boolean catalogQuestion, boolean requestedAvailability, boolean specificHotel,
                    String city, String state, String hotelName, String roomType,
                    boolean sortByPrice, BigDecimal maxPrice, Double minRating,
                    LocalDate checkIn, LocalDate checkOut) {
            this.catalogQuestion = catalogQuestion;
            this.requestedAvailability = requestedAvailability;
            this.specificHotel = specificHotel;
            this.city = city;
            this.state = state;
            this.hotelName = hotelName;
            this.roomType = roomType;
            this.sortByPrice = sortByPrice;
            this.maxPrice = maxPrice;
            this.minRating = minRating;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }

        static SearchQuery from(String message, List<Hotel> hotels) {
            String lower = message.toLowerCase(Locale.ROOT);
            String city = known(lower, hotels, Hotel::getCity);
            String state = known(lower, hotels, Hotel::getState);
            String hotel = known(lower, hotels, Hotel::getHotelName);
            String type = roomType(lower);
            LocalDate[] dates = dates(lower);
            BigDecimal price = price(lower);
            Double rating = rating(lower);
            boolean available = contains(lower, "available", "availability", "vacancy", "vacant", "free room", "rooms open");
            boolean catalog = available || city != null || state != null || hotel != null || type != null
                    || price != null || rating != null || contains(lower, "hotel", "hotels", "room", "rooms", "stay", "stays", "rating", "rated", "price", "cheap", "budget", "top rated", "best");
            boolean sortPrice = contains(lower, "cheap", "cheapest", "lowest price", "budget", "price low", "sort by price");
            return new SearchQuery(catalog, available, hotel != null, city, state, hotel, type,
                    sortPrice, price, rating, dates[0], dates[1]);
        }

        boolean hasDates() { return checkIn != null && checkOut != null; }

        String summary() {
            return "city=" + safe(city) + ", state=" + safe(state) + ", hotel=" + safe(hotelName)
                    + ", roomType=" + safe(roomType) + ", dates="
                    + (hasDates() ? checkIn + " to " + checkOut : "not supplied")
                    + ", availability=" + requestedAvailability + ", sort=" + (sortByPrice ? "price" : "rating");
        }

        private static String known(String lower, List<Hotel> hotels, Function<Hotel, String> field) {
            return hotels.stream().map(field).filter(v -> v != null && !v.isBlank()).distinct()
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .filter(v -> lower.contains(v.toLowerCase(Locale.ROOT))).findFirst().orElse(null);
        }

        private static String roomType(String lower) {
            for (String type : List.of("suite", "deluxe", "standard", "single", "double", "family", "executive"))
                if (lower.contains(type)) return type;
            return null;
        }

        private static LocalDate[] dates(String lower) {
            Matcher matcher = ISO_DATE.matcher(lower);
            List<LocalDate> parsed = new ArrayList<>();
            while (matcher.find() && parsed.size() < 2) {
                try { parsed.add(LocalDate.parse(matcher.group(1))); }
                catch (DateTimeParseException ignored) { }
            }
            return parsed.size() == 2 && parsed.get(1).isAfter(parsed.get(0))
                    ? new LocalDate[]{parsed.get(0), parsed.get(1)} : new LocalDate[]{null, null};
        }

        private static BigDecimal price(String lower) {
            Matcher matcher = PRICE.matcher(lower);
            if (!matcher.find()) return null;
            try {
                BigDecimal value = new BigDecimal(matcher.group(1).replace(",", ""));
                return "k".equalsIgnoreCase(matcher.group(2)) ? value.multiply(BigDecimal.valueOf(1000)) : value;
            } catch (NumberFormatException ignored) { return null; }
        }

        private static Double rating(String lower) {
            Matcher matcher = RATING.matcher(lower);
            if (!matcher.find()) return null;
            try {
                double value = Double.parseDouble(matcher.group(1));
                return value >= 0 && value <= 5 ? value : null;
            } catch (NumberFormatException ignored) { return null; }
        }

        private static boolean contains(String text, String... terms) {
            for (String term : terms) if (text.contains(term)) return true;
            return false;
        }

        private static String safe(String value) { return value == null ? "any" : value; }
    }
}
