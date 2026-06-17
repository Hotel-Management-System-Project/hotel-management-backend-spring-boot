package utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Resp<T> {
public static enum Status{
	success,error
}

private Status status;
private Object data;
private String message ;



public static <T> Resp<T> success(T data){
	return new Resp<T>(Status.success,data,null);
}


public static <T> Resp<T> error(String message){
	return new Resp<T> (Status.error,null,message);
}
}
