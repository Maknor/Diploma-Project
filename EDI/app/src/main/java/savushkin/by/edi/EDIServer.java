package savushkin.by.edi;

import android.content.Context;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

public class EDIServer {

    private EDIServerListener callback;
    private String NAMESPACE;
    private String URL;
    private SoapObject request;
    private PropertyInfo piUserName;
    private PropertyInfo piPassword;

    public EDIServer(Context base, String login, String pass, String namespace, String url) {
        this.callback = (EDIServerListener) base;
        setUserPassword(login, pass);
        this.NAMESPACE = namespace;
        this.URL = url;
    }


    public void queryDocuments(String dType) {
        if(dType != null) {
            PropertyInfo pi1 = new PropertyInfo();
            pi1.name = "documentType";
            pi1.type = PropertyInfo.STRING_CLASS;
            pi1.setValue(dType);

            List<PropertyInfo> properties = new ArrayList<>();
            properties.add(pi1);
            call(Commands.QueryDocuments, properties);
        } else {
            call(Commands.QueryDocuments, null);
        }
    }

    public void getDocuments(List<String> docsIds) {
        if(docsIds != null) {
            List<PropertyInfo> properties = new ArrayList<>();
            for (String id : docsIds) {
                PropertyInfo pi1 = new PropertyInfo();
                pi1.name = "documentIds";
                pi1.type = PropertyInfo.STRING_CLASS;
                pi1.setValue(id);
                properties.add(pi1);
            }
            call(Commands.GetDocuments, properties);
        } else {
            call(Commands.GetDocuments, null);
        }
    }

    public void confirmDocumentReceived(ArrayList<String> docs) {
        List<PropertyInfo> properties = new ArrayList<>();
        for (String doc : docs) {
            PropertyInfo pi1 = new PropertyInfo();
            pi1.name = "documentId";
            pi1.type = PropertyInfo.STRING_CLASS;
            pi1.setValue(doc);
            properties.add(pi1);
        }
        call(Commands.ConfirmDocumentReceived, properties);
    }

    public void sendDocument(String documentType, String document) {
        PropertyInfo pi1 = new PropertyInfo();
        pi1.name = "documentType";
        pi1.type = PropertyInfo.STRING_CLASS;
        pi1.setValue(documentType);

        PropertyInfo pi2 = new PropertyInfo();
        pi2.name = "content";
        pi2.type = PropertyInfo.STRING_CLASS;
        pi2.setValue(Converter.getToBase64(document));//отправляем документ на сервер сконвертированный в base64

        List<PropertyInfo> properties = new ArrayList<>();
        properties.add(pi1);
        properties.add(pi2);

        call(Commands.SendDocument, properties);
    }

    void setUserPassword(String username, String password) {
        piUserName = new PropertyInfo();
        piUserName.name = "username";
        piUserName.type = PropertyInfo.STRING_CLASS;
        piUserName.setValue(username);

        piPassword = new PropertyInfo();
        piPassword.name = "password";
        piPassword.type = PropertyInfo.STRING_CLASS;
        piPassword.setValue(password);
    }

    private void call(Commands method, List<PropertyInfo> properties) {
        Object response = null;
        String METHOD_NAME = method.toString();
        Log.e("Call","= Start");//документ пришел на сервер
        try {
            request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty(piUserName);
            request.addProperty(piPassword);

            if(properties != null) {
                for (PropertyInfo propertyInfo : properties) {
                    request.addProperty(propertyInfo);
                }
            }

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);
            envelope.dotNet = true;

            Log.e("URL","=="+URL);
            Log.e("NAMESPACE","=="+NAMESPACE);
            HttpTransportSE ht = new HttpTransportSE(URL);
            ht.call(NAMESPACE + METHOD_NAME, envelope);
            response = envelope.getResponse();
            Log.e("Envelope","="+envelope.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(response == null) {
            callback.error("null", method);
            return;
        }
        Log.e("Soap","="+response.toString());
        SoapObject so = (SoapObject) response;
        String successful = so.getPropertyAsString("Succesful");
        if(successful.equals("false")) {
            callback.error(so.getPropertyAsString("ErrorCode") + " " +
                    so.getPropertyAsString("Message"), method);
            return;
        }
        Log.e("Call","= End");//документ ушел с сервера
        // В so содержится ответ от сервера.
        switch (method) {
            case GetDocuments:
                callback.getDocuments(so);
                break;
            case QueryDocuments:
                callback.queryDocuments(so);
                break;
            case SendDocument:
                callback.sendDocument(so);
                break;
            case ConfirmDocumentReceived:
                callback.confirmDocumentReceived(so);
                break;
        }
    }

    public enum Commands {
        // Получение информации и содержимого документа (открытие документа).
        GetDocuments,
        // Получаем список документов с сервера.
        QueryDocuments,
        // Автоматическое подтверждение получения документа.
        ConfirmDocumentReceived,
        // Отправка документа на сервер.
        SendDocument;
    }

    public enum DocumentType {//все типы документов
        BLRAPN,
        BLRDLN,
        BLRWBL,
        BLRWBR,
        BLRDNR,
        ORDERS,
        ORDRSP,
        DESADV,
        APERAK,
        PRICAT,
        PROINQ,
        INVOIC,
        RECADV,
        INVRPT,
        SLSRPT,
        CONFIG;
    }

    public interface EDIServerListener {
        void queryDocuments(SoapObject so);//запрос к документу на сервере(получить список документов)
        void getDocuments(SoapObject so);//получить содержимое документа с сервера
        void confirmDocumentReceived(SoapObject so);//подтвердить полученный документ
        void sendDocument(SoapObject so);//отправить документ на сервер
        void error(String error, Commands command);//ошибка
    }
}
