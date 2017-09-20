package savushkin.by.edi;

public class State {

    private String nameDoc;
    private String numDoc;
    private String dataDoc;

    public State(String nameDoc, String numDoc, String dataDoc) {
        this.nameDoc = nameDoc;
        this.numDoc = numDoc;
        this.dataDoc = dataDoc;
    }

    public String getNameDoc(){
        return this.nameDoc;
    }
    public void setNameDoc(String nameDoc){
        this.nameDoc = nameDoc;
    }
    public String getNumDoc(){
        return  this.numDoc;
    }
    public void setNumDoc(String numDoc){
        this.numDoc = numDoc;
    }
    public String getDataDoc(){
        return this.dataDoc;
    }
    public void setDataDoc(String dataDoc){
        this.dataDoc = dataDoc;
    }

}