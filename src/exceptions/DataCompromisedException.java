package exceptions;

public class DataCompromisedException extends Exception{
    public DataCompromisedException(){
        super();
    }
    public DataCompromisedException(String message){
        super(message);
    }
}
