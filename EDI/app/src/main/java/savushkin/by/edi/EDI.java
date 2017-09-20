package savushkin.by.edi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import by.savushkin.xmleditor.xmleditor.Other.FilesHelper;
import by.savushkin.xmleditor.xmleditor.Other.onXmlEditorResult;
import by.savushkin.xmleditor.xmleditor.XmlEditor;

import static savushkin.by.edi.Settings.APP_PREFERENCES_Login;

public class EDI extends AppCompatActivity implements EDIServer.EDIServerListener {

    // данные для входа на сервер
    String login;// = "savushkin";
    String pass;// = "MMGefrK9B";
    String NAMESPACE = "http://topby.by/";
    String URL = "http://topby.by:1984/DmcService?WSDL";

    final String LOG_TAG = "myLogs";
    ArrayList<String> ids = null; // доп. параметры для GetDocuments
    String dType = null; // доп. параметры для QueryDocuments
    ArrayList<String> confirmDocs = new ArrayList<>();// доп. параметры для ConfirmDocumentReceived
    ArrayList<String> confirmDocs1 = new ArrayList<>();
    String documentType = ""; // доп. параметры для SendDocument
    String document = ""; // доп. параметры для SendDocument
    ArrayList<String> doc = new ArrayList<>();
    ArrayList<String> xml = new ArrayList<>();
    ArrayList<String> docType = new ArrayList<>();
    SoapObject documentData;
    XmlEditor xmlEditor;
    List<State> states = new ArrayList();
    String docTypeid;
    String xmlid;
    String filenameid;
    public static String fSize;
    public static String style;
    public static Boolean fcolor;
    SharedPreferences pref;
    SharedPreferences mSettings;
    EDIServer.Commands command;
    TextView name, num, data;
    ListView listView1;
    ProgressDialog pd;

    enum DocumentType1 {
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
        PROINQ, ///--------------------------------------------------------------
        INVOIC,
        RECADV,
        INVRPT,
        SLSRPT,
        CONFIG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        login = getIntent().getStringExtra("Login");
        pass = getIntent().getStringExtra("Password");
        //NAMESPACE = getIntent().getStringExtra("ServerName");
        //URL = getIntent().getStringExtra("URL");
        Log.d(LOG_TAG, "Login=" + login);
        Log.d(LOG_TAG, "Password=" + pass);
        Log.d(LOG_TAG, "ServerName=" + NAMESPACE);
        Log.d(LOG_TAG, "URL=" + URL);

        name = (TextView) findViewById(R.id.nameView);
        num = (TextView) findViewById(R.id.numView);
        data = (TextView) findViewById(R.id.dataView);

        try {
            File path = Environment.getExternalStorageDirectory();
            path = new File(path.getAbsolutePath() + "/SaveListXML");
            path.mkdirs();
            Log.d(LOG_TAG, "Папка создана");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //getDoc();
        //queryDoc();
        //confirmDoc();
        //sendDoc();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        fcolor = pref.getBoolean("night", false);
        //режим для вида поля ввода (ночной дневной)
        if (pref.getBoolean("night", false)) {// night - наше поле в settings.xml, false - не активно.
            name.setBackgroundColor(Color.RED);  //задаем красный цвет фона для edittext
            name.setTextColor(Color.CYAN);       //задаем синий цвет текста
            num.setBackgroundColor(Color.RED);
            num.setTextColor(Color.BLACK);
            data.setBackgroundColor(Color.RED);
            data.setTextColor(Color.CYAN);


        } else {
            name.setBackgroundColor(Color.YELLOW);
            name.setTextColor(Color.BLUE);
            num.setBackgroundColor(Color.YELLOW);
            num.setTextColor(Color.MAGENTA);
            data.setBackgroundColor(Color.YELLOW);
            data.setTextColor(Color.BLUE);

        }
        fSize = pref.getString(getString(R.string.pref_size), "20");

        name.setTextSize(Float.parseFloat(fSize));
        num.setTextSize(Float.parseFloat(fSize));
        data.setTextSize(Float.parseFloat(fSize));

        style =  pref.getString(getString(R.string.pref_style), "");
        int typeface = Typeface.NORMAL;

        if (style.contains("Полужирный"))
            typeface += Typeface.BOLD;

        if (style.contains("Курсив"))
            typeface += Typeface.ITALIC;
        name.setTypeface(null, typeface);
        num.setTypeface(null, typeface);
        data.setTypeface(null, typeface);

        mSettings = getSharedPreferences("login.conf", Context.MODE_PRIVATE);
        login = mSettings.getString("Login", APP_PREFERENCES_Login);
        pass = getIntent().getStringExtra("Password");
        Log.d(LOG_TAG, "NewLog=" + login);

        if (Settings.chekSettings) {
            clearList();
            states.clear();
            getDoc();
            queryDoc();
            Settings.chekSettings = false;
        }
    }

    private void clearList() {
        List<String> list = new ArrayList<String>();
        ArrayAdapter<String> newAdapter = new ArrayAdapter<String>(this, R.layout.list_item, list);
        ListView myList = (ListView) findViewById(R.id.ListView1);
        myList.setAdapter(newAdapter);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    //метод запуск edi сервера
    public void runEDI(EDIServer.Commands command) {
        switch (command) {
            case GetDocuments://получаем документ
                new Thread() {
                    public void run() {
                        new EDIServer(EDI.this, login, pass, NAMESPACE, URL).getDocuments(ids);
                    }
                }.start();
                break;

            case QueryDocuments: //выполняем запрос к документу
                new Thread() {
                    public void run() {
                        if (dType != null) {
                            new EDIServer(EDI.this, login, pass, NAMESPACE, URL).queryDocuments(dType);//запрос с параметром dType
                        } else {
                            new EDIServer(EDI.this, login, pass, NAMESPACE, URL).queryDocuments(null);//запрос без параметра
                        }
                    }
                }.start();
                break;

            case ConfirmDocumentReceived:
                new Thread() {
                    public void run() {
                        new EDIServer(EDI.this, login, pass, NAMESPACE, URL).confirmDocumentReceived(confirmDocs);
                    }
                }.start();
                break;

            case SendDocument:
                new Thread(){
                    public void run() {
                        new EDIServer(EDI.this, login, pass,
                                NAMESPACE, URL).sendDocument(documentType, document);
                    }}.start();
                break;
        }
    }


    public void getDocuments(SoapObject so) {
        Intent intent = new Intent();
        intent.putExtra("module", "EDI");
        intent.putExtra("command", "GetDocuments");
        StringBuilder result = new StringBuilder();
        SoapObject data = (SoapObject) so.getProperty("Data");
        states.clear();
        confirmDocs1.clear();

        result.append("<Documents>");
        for (int i = 0; i < data.getPropertyCount(); i++) {
            documentData = (SoapObject) data.getProperty(i); // DocumentData(данные документа)
            result.append("<DocumentData>");

            //result.append("<FullDocument>" + documentData.toString() + "</FullDocument>");
            intent.putExtra("xml", Converter.getFromBase64(documentData.getPropertyAsString("Data")));//помещаем в интент обьект xml с конвертированной в base64 documentData с параметром.
            result.append("<Data>" + Converter.getFromBase64(documentData.getPropertyAsString("Data")) + "</Data>");
            result.append("<Id>" + documentData.getProperty("Id").toString() + "</Id>");
            Log.e("ID", "=" + documentData.getProperty("Id").toString());//вывести результат в логе с ID
            result.append("<DocumentDate>" + documentData.getPropertyAsString("DocumentDate") + "</DocumentDate>");
            Log.e("DocumentDate", "=" + documentData.getPropertyAsString("DocumentDate"));//вывести результат в логе с DocumentDate(дата документа)
            result.append("<ModifiedDate>" + documentData.getPropertyAsString("ModifiedDate") + "</ModifiedDate>");
            Log.e("ModifiedDate", "=" + documentData.getPropertyAsString("ModifiedDate"));//вывести результат в логе с ModifiedDate(дата изменения)
            result.append("<DocumentNumber>" + documentData.getPropertyAsString("DocumentNumber") + "</DocumentNumber>");
            Log.e("DocumentNumber", "=" + documentData.getPropertyAsString("DocumentNumber"));//вывести результат в логе с DocumentNumber(номер документа)
            intent.putExtra("docid", documentData.getPropertyAsString("Filename"));//помещаем в интент обьект docid с параметром имя файла
            intent.putExtra("doctype", documentData.getPropertyAsString("DocumentType"));//помещаем в интент обьект doctype с параметром тип документа
            result.append("<Filename>" + documentData.getPropertyAsString("Filename") + "</Filename>");
            result.append("<DocumentType>" + documentData.getPropertyAsString("DocumentType") + "</DocumentType>");
            result.append("<ReadOnWeb>" + documentData.getPropertyAsString("ReadOnWeb") + "</ReadOnWeb>");
            result.append("<GotByAgent>" + documentData.getPropertyAsString("GotByAgent") + "</GotByAgent>");
            result.append("<ApprovalStatus>" + documentData.getPropertyAsString("ApprovalStatus") + "</ApprovalStatus>");
            result.append("<ProcessingStatus>" + documentData.getPropertyAsString("ProcessingStatus") + "</ProcessingStatus>");
            Log.e("Filename", "=" + documentData.getPropertyAsString("Filename"));//вывести результат в логе имя файла
            Log.e("DocumentType", "=" + documentData.getPropertyAsString("DocumentType"));//вывести результат в логе тип документа
            Log.e("ReadOnWeb", "=" + documentData.getPropertyAsString("ReadOnWeb"));//вывести результат в логе чтение из веб
            Log.e("GotByAgent", "=" + documentData.getPropertyAsString("GotByAgent"));//вывести результат в логе получение агента
            Log.e("ApprovalStatus", "=" + documentData.getPropertyAsString("ApprovalStatus"));//вывести результат в логе статус одобрения
            Log.e("ProcessingStatus", "=" + documentData.getPropertyAsString("ProcessingStatus"));//вывести результат в логе статус обработки
            result.append("</DocumentData>");
            doc.add(documentData.getPropertyAsString("Filename"));
            docType.add(documentData.getPropertyAsString("DocumentType"));
            String doc1 = documentData.getPropertyAsString("Filename");
            String data2= documentData.getPropertyAsString("DocumentDate");
            DocumentType1 doctype2;
            doctype2 = DocumentType1.valueOf(documentData.getPropertyAsString("DocumentType").toUpperCase());;
            String DocTy= "";
            switch (doctype2){
                case BLRAPN:
                    DocTy = "Сообщение извещения или подтверждения";
                    break;
                case BLRDLN:
                    DocTy = "Электронное сообщение Товарная Накладная в Беларуси";
                    break;
                case BLRWBL:
                    DocTy = "Электронное сообщение Товарно транспортная накладная в Беларуси";
                    break;
                case BLRWBR:
                    DocTy = "Электронное сообщение Товарно-Транспортная Накладная в Беларуси (ответ грузополучателя)";
                    break;
                case BLRDNR:
                    DocTy = "Электронное сообщение Товарная Накладная в Беларуси ответ грузополучателя";
                    break;
                case ORDERS:
                    DocTy = "Заказ для стимы";
                    break;
                case ORDRSP:
                    DocTy = "Ответ на заказ";
                    break;
                case DESADV:
                    DocTy = "Уведомление об отгрузке";
                    break;
                case APERAK:
                case PRICAT:
                    DocTy = "Тип документа PRICAT";
                    break;
                case PROINQ:
                    DocTy = "Запрос на получение приката";
                    break;
                case INVOIC:
                case RECADV:
                case INVRPT:
                case SLSRPT:
                case CONFIG:
            }
            states.add(new State(DocTy, doc1.substring(7,20), data2.substring(0, 10)));
            confirmDocs1.add(documentData.getProperty("Id").toString());
            Log.d(LOG_TAG, "Add" + confirmDocs1.toString());
            writeFile(Converter.getFromBase64(documentData.getPropertyAsString("Data")),
                    documentData.getPropertyAsString("Filename"));

        }


        result.append("<Documents>");
        intent.putExtra("result", intent.getStringExtra("xml"));
        setResult(RESULT_OK, intent);
        dumpIntent(intent);
        pd.dismiss();
        refresh2();
        //EDI.this.finish();
    }
    private void writeFile(String xml, String filename) {
        try {
            File newFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SaveListXML/" + filename);
            newFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(newFile);
            outputStream.write(xml.getBytes());
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void queryDocuments(SoapObject so) {
        Intent intent = new Intent();
        intent.putExtra("module", "EDI");
        intent.putExtra("command", "QueryDocuments");
        StringBuilder result = new StringBuilder();
        result.append("<DOCLIST>");
        if (so.getPropertyCount() > 3) {
            for (int i = 3; i < so.getPropertyCount(); i++) {
                String[] st = so.getPropertyAsString(i).split("\\_");
                String schema = "";
                String numberDocument = "";
                switch (EDIServer.DocumentType.valueOf(st[0].toUpperCase())) {
                    case BLRAPN:
                    case BLRDLN:
                    case BLRWBL:
                    case BLRWBR:
                    case BLRDNR:
                    case ORDERS:
                    case ORDRSP:
                    case DESADV:
                    case APERAK:
                    case PRICAT:
                    case PROINQ:
                    case INVOIC:
                    case RECADV:
                    case INVRPT:
                    case SLSRPT:
                    case CONFIG:
                        schema = st[0].toLowerCase();
                        break;
                    default:
                        schema = st[0];
                }
                if (st.length >= 2) {
                    if (st.length == 2) {
                        numberDocument = st[1].split("\\.")[0];
                    } else {
                        numberDocument = st[1];
                    }
                }
                result.append("<DATA>" + "<DOCID>" + so.getPropertyAsString(i) + "</DOCID>" +
                        "<TD>" + schema + "</TD>" + "<STATUS> </STATUS> <STATUS2> </STATUS2>" +
                        "<ND>" + numberDocument + "</ND>" + "</DATA>");

            }
        }
        result.append("</DOCLIST>");
        intent.putExtra("result", result.toString());
        Log.e("LOG", "==" + result.toString());
        setResult(RESULT_OK, intent);
        dumpIntent(intent);
        //EDI.this.finish();
    }

    public void confirmDocumentReceived(SoapObject so) {
        Log.e("confirmDocumentReceived", so.toString());//вывести результат в логе подтверждаем полученный документ
        Intent intent = new Intent();
        intent.putExtra("module", "EDI");
        intent.putExtra("command", "ConfirmDocumentReceived");
        intent.putExtra("result", so.toString());
        setResult(RESULT_OK, intent);
        dumpIntent(intent);
        //EDI.this.finish();
    }

    public void sendDocument(SoapObject so) {
        Log.e("sendDocument", so.toString());//вывести результат в логе отправляем документ
        Intent intent = new Intent();
        intent.putExtra("module", "EDI");
        intent.putExtra("command", "SendDocument");
        intent.putExtra("result", so.toString());
        String successful = so.getPropertyAsString("Succesful");
        if (successful.equals("true")) {
            Intent toast = new Intent();
            toast.putExtra("1", "Документ успешно отправлен");
            treadToast(toast);
        }
        setResult(RESULT_OK, intent);
        dumpIntent(intent);
        //EDI.this.finish();
    }
    private void treadToast(final Intent toast) {
        runOnUiThread(new Runnable() {
            public void run() {
                Bundle bundle = toast.getExtras();
                if (bundle != null) {
                    Set<String> keys = bundle.keySet();
                    Iterator<String> it = keys.iterator();
                    String key = it.next();
                    Toast.makeText(EDI.this, bundle.get(key).toString(), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public void error(String msg, EDIServer.Commands error) {
        Intent intent = new Intent();
        intent.putExtra("module", "EDI");
        intent.putExtra("command", "error");
        intent.putExtra("msg", msg);
        Log.e("MSG", "ERROR");
        setResult(RESULT_OK, intent);
        dumpIntent(intent);
        Intent toast = new Intent();
        toast.putExtra("msg", msg);
        treadToast(toast);
        //EDI.this.finish();
    }

    public void dumpIntent(Intent i) {//завершение интента
        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.e("dumpIntent", "Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                Log.e("dumpIntent", "[" + key + "=" + bundle.get(key) + "]");
            }
            Log.e("dumpIntent", "Dumping Intent end");
        }
    }

    @Override
    public void onBackPressed() {//кнопка назад
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Toast.makeText(EDI.this, getString(R.string.action_settings), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Настройки");
                Intent intent2 = new Intent(EDI.this, Settings.class);
                startActivity(intent2);
                break;
            case R.id.action_refresh:
                Toast.makeText(EDI.this, getString(R.string.action_refresh), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Обновить");
                refresh();
                break;
            case R.id.action_information:
                Toast.makeText(EDI.this, getString(R.string.action_information), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Информация");
                Intent intent = new Intent(EDI.this, Inform.class);
                startActivity(intent);
                break;
            case R.id.action_close:
                Toast.makeText(EDI.this, getString(R.string.action_close), Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor;
                SharedPreferences mSettings;
                mSettings = getSharedPreferences("login.conf", Context.MODE_PRIVATE);
                editor = mSettings.edit();
                editor.clear();
                editor.apply();
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void refresh() {
        pd = new ProgressDialog(this);
        pd.setMessage("Обновление");
        pd.show();
        getDoc();

    }
    private void refresh2() {
        runOnUiThread(new Runnable() {
                          public void run() {
                              listView1 = (ListView) findViewById(R.id.ListView1);
                              StateAdapter adapter = new StateAdapter(EDI.this, R.layout.list_item, states);
                              listView1.setAdapter(adapter);
                              registerForContextMenu(listView1);
                              listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                  @Override
                                  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                      filenameid = doc.get(position);
                                      Log.d(LOG_TAG, "filemameid=" + filenameid);
                                      docTypeid = docType.get(position);
                                      startRedactor(docTypeid, filenameid);
                                  }
                              });
                          }
                      }
        );
    }
    private void startRedactor(final String docTypeid, final String filenameid){
    xmlEditor = new XmlEditor(EDI.this);
        final String testDir1 =  Environment.getExternalStorageDirectory() + "/SaveListXML/" ;
        final String testDir = Environment.getExternalStorageDirectory() + "/edi/";
        Log.d(LOG_TAG, "XMLID=" + xmlid);
        Log.d(LOG_TAG, "DocType=" + docTypeid);
        Log.d(LOG_TAG, "Filenameid=" + filenameid);
        Log.d(LOG_TAG, "TestDir=" + testDir + docTypeid + ".xsd");
        xmlEditor.editDoc(FilesHelper.read(testDir1 + filenameid), FilesHelper.read(testDir + docTypeid + ".xsd"), true, filenameid,
                new onXmlEditorResult() {
                    @Override
                    public void onResult(String result) {
                        FilesHelper.write(testDir1 + filenameid, result);
                    }
                }, testDir, null, testDir);
}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo MenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.open:
                Toast.makeText(EDI.this, getString(R.string.action_open), Toast.LENGTH_SHORT).show();
                startRedactor(docType.get(MenuInfo.position),doc.get(MenuInfo.position));
                break;
            case R.id.send:
                Toast.makeText(EDI.this, getString(R.string.action_send), Toast.LENGTH_SHORT).show();
                documentType = docType.get(MenuInfo.position);
                final String testDir1 = Environment.getExternalStorageDirectory() + "/SaveListXML/";
                document = FilesHelper.read(testDir1 + doc.get(MenuInfo.position));
                Log.d(LOG_TAG, "Document" + document);
                sendDoc();
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void getDoc() {
        command = EDIServer.Commands.GetDocuments;
        ids = null;
        runEDI(command);
    }

    private void queryDoc() {
        command = EDIServer.Commands.QueryDocuments;
        dType = null;
        runEDI(command);
    }

    private void confirmDoc() {
        command = EDIServer.Commands.ConfirmDocumentReceived;
        confirmDocs = confirmDocs1;
        runEDI(command);
    }

    private void sendDoc() {
        command = EDIServer.Commands.SendDocument;
        runEDI(command);
    }

}