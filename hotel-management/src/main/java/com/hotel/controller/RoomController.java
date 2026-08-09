	package com.hotel.controller;
	
	import java.util.List;
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.web.bind.annotation.DeleteMapping;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.PatchMapping;
	import org.springframework.web.bind.annotation.PathVariable;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.PutMapping;
	import org.springframework.web.bind.annotation.RequestBody;
	import org.springframework.web.bind.annotation.RestController;
	import org.springframework.security.core.Authentication;
	
	import com.hotel.dto.RoomRequestDTO;
	import com.hotel.dto.RoomResponseDTO;
	import com.hotel.dto.BulkRoomRequestDTO;
	import com.hotel.service.RoomService;
	import com.hotel.utils.Resp;
	
	import jakarta.validation.Valid;
	
	
	
	
	@RestController
	public class RoomController {
	
		@Autowired
		private RoomService roomService;
	
		@PostMapping("/addRoom")
		public Resp<RoomResponseDTO> addRoom(@RequestBody RoomRequestDTO dto) {
			RoomResponseDTO response = roomService.addRoom(dto);
			System.out.println("Inside addRoom");
	//		System.out.println("Response" + response);
			return Resp.success(response);
		}

		@PostMapping("/addRoomsBulk")
		public Resp<List<RoomResponseDTO>> addRoomsBulk(
				@Valid @RequestBody BulkRoomRequestDTO dto,
				Authentication authentication) {
			if (authentication == null) {
				return Resp.error("Unauthorized");
			}

			boolean isAdmin = authentication.getAuthorities().stream()
					.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
			return Resp.success(roomService.addRoomsInBulk(
					dto, authentication.getName(), isAdmin
			));
		}
	
		
		
		@GetMapping("/getAllRooms")
		public Resp<List<RoomResponseDTO>> getAllRooms(RoomResponseDTO dto){
			List<RoomResponseDTO> roomList=roomService.getAllRooms();
			return  Resp.success(roomList);
		}
		
		@DeleteMapping("/deleteRoom/{roomNumber}")
		public Resp<RoomResponseDTO> deleteByRoomNumber(@PathVariable Integer roomNumber) {
	
		    RoomResponseDTO deletedRoom = roomService.deleteByRoomNumber(roomNumber);
	
		    return Resp.success(deletedRoom);
		}
		
		
		@GetMapping("/getRoomById/{id}")
		public Resp<RoomResponseDTO> getRoomById(@PathVariable("id") Integer id) {

		    System.out.println("Inside getRoomById : " + id);

		    RoomResponseDTO room = roomService.findRoomById(id);

		    return Resp.success(room);
		}
	
		
		@PutMapping("/updateByRoomNumber/{roomNumber}")
		public Resp<RoomResponseDTO> updateRoomByRoomNumber(
		        @PathVariable Integer roomNumber,
		        @Valid @RequestBody RoomRequestDTO dto) {
	
		    RoomResponseDTO updatedRoom =
		            roomService.updateRoomByRoomNumber(roomNumber, dto);
	
		    return Resp.success(updatedRoom);
		}
		
		
		
		//find by room type 
		@GetMapping("/getRoomByType/{roomType}")
		public Resp<List<RoomResponseDTO>> getRoomByType(
		        @PathVariable String roomType) {
	
		    List<RoomResponseDTO> rooms =
		            roomService.findRoomByType(roomType);
	
		    return Resp.success(rooms);
		}
		
		@PutMapping("/updateById/{id}")
		public Resp<RoomResponseDTO> updateRoomById(
		        @PathVariable Integer id,
		        @Valid @RequestBody RoomRequestDTO dto,
		        Authentication authentication
		) {
		    if (authentication == null
		            || !authentication.isAuthenticated()) {
		        return Resp.error("Unauthorized");
		    }

		    boolean allowed = authentication
		            .getAuthorities()
		            .stream()
		            .anyMatch(authority ->
			        authority.getAuthority()
			                        .equals("ROLE_HOTEL_OWNER")
			                || authority.getAuthority()
			                        .equals("ROLE_OWNER")
			                || authority.getAuthority()
			                        .equals("ROLE_ADMIN")
		            );

		    if (!allowed) {
		        return Resp.error(
		            "Only hotel owners and administrators can update rooms"
		        );
		    }

		    RoomResponseDTO updatedRoom =
		            roomService.updateRoomById(id, dto);

		    return Resp.success(updatedRoom);
		}
		
		//delete by room id
		@DeleteMapping("/deleteRoomById/{id}")
		public Resp<RoomResponseDTO> deleteRoomById(
		        @PathVariable Integer id) {
	
		    RoomResponseDTO deletedRoom =
		            roomService.deleteRoomById(id);
	
		    return Resp.success(deletedRoom);
		}
		
		
		
		@GetMapping("/getRoomByRoomNumber/{roomNumber}")
		public Resp<RoomResponseDTO> getRoomByRoomNumber(@PathVariable Integer roomNumber) {
	
		    RoomResponseDTO room = roomService.findRoomByRoomNumber(roomNumber);
	
		    return Resp.success(room);
		}
		
		
	}
