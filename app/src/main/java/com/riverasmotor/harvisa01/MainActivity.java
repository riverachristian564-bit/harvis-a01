package com.riverasmotor.harvisa01;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    EditText baseUrl, token, message; TextView status, chat; ScrollView scroll; TextToSpeech tts; final int VOICE=42;
    @Override protected void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_main);
        baseUrl=findViewById(R.id.baseUrl); token=findViewById(R.id.token); message=findViewById(R.id.message); status=findViewById(R.id.status); chat=findViewById(R.id.chat); scroll=findViewById(R.id.scroll);
        android.content.SharedPreferences p=getSharedPreferences("harvis",0); baseUrl.setText(p.getString("url","http://192.168.1.2:8765")); token.setText(p.getString("token",""));
        tts=new TextToSpeech(this,this);
        findViewById(R.id.connect).setOnClickListener(v->health(true)); findViewById(R.id.send).setOnClickListener(v->send());
        findViewById(R.id.stop).setOnClickListener(v->{if(tts!=null)tts.stop();status.setText("CONECTADO");}); findViewById(R.id.mic).setOnClickListener(v->listen());
        health(false);
    }
    @Override public void onInit(int s){if(s==TextToSpeech.SUCCESS)tts.setLanguage(new Locale("es","AR"));}
    void save(){getSharedPreferences("harvis",0).edit().putString("url",baseUrl.getText().toString().trim()).putString("token",token.getText().toString()).apply();}
    HttpURLConnection conn(String path,String method)throws Exception{String root=baseUrl.getText().toString().trim().replaceAll("/+$","");if(root.length()==0)throw new Exception("Falta URL del Core");URL u=new URL(root+path);HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(5000);c.setReadTimeout(120000);c.setRequestMethod(method);c.setRequestProperty("Content-Type","application/json; charset=utf-8");String t=token.getText().toString().trim();if(!t.isEmpty()){c.setRequestProperty("Authorization","Bearer "+t);c.setRequestProperty("X-HARVIS-Token",t);}return c;}
    void health(boolean load){save();ui(()->status.setText("CONECTANDO…"));new Thread(()->{try{HttpURLConnection c=conn("/health","GET");String s=read(c);int code=c.getResponseCode();if(code<400){ui(()->status.setText("CONECTADO"));if(load)bootstrap();}else fail("HTTP "+code+" "+s);}catch(Exception e){fail(e.getMessage());}}).start();}
    void bootstrap(){new Thread(()->{try{HttpURLConnection c=conn("/v1/bootstrap","GET");String s=read(c);if(c.getResponseCode()>=400)return;JSONObject j=new JSONObject(s);JSONArray h=j.optJSONArray("history");StringBuilder b=new StringBuilder();if(h!=null){int start=Math.max(0,h.length()-20);for(int i=start;i<h.length();i++){JSONObject x=h.optJSONObject(i);if(x==null)continue;String role=x.optString("role");String content=x.optString("content",x.optString("text",""));b.append("user".equals(role)?"Vos: ":"HARVIS: ").append(content).append("\n\n");}}ui(()->{chat.setText(b.toString());bottom();});}catch(Exception ignored){}}).start();}
    void send(){String m=message.getText().toString().trim();if(m.isEmpty())return;save();message.setText("");append("Vos: "+m+"\n\n");ui(()->status.setText("PROCESANDO…"));new Thread(()->{try{JSONObject j=new JSONObject();j.put("message",m);j.put("text",m);j.put("device","android-a01");HttpURLConnection c=conn("/v1/chat","POST");c.setDoOutput(true);try(OutputStream o=c.getOutputStream()){o.write(j.toString().getBytes(StandardCharsets.UTF_8));}String raw=read(c);if(c.getResponseCode()>=400)throw new Exception("HTTP "+c.getResponseCode()+" "+raw);JSONObject r=new JSONObject(raw);String a=r.optString("answer",r.optString("response",r.optString("text","")));if(a.trim().isEmpty())a=raw;final String ans=a;ui(()->{append("HARVIS: "+ans+"\n\n");status.setText("HABLANDO");if(tts!=null)tts.speak(ans,TextToSpeech.QUEUE_FLUSH,null,"harvis");});}catch(Exception e){fail(e.getMessage());}}).start();}
    void listen(){if(Build.VERSION.SDK_INT>=23&&checkSelfPermission("android.permission.RECORD_AUDIO")!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{"android.permission.RECORD_AUDIO"},7);return;}try{Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"es-AR");status.setText("ESCUCHANDO…");startActivityForResult(i,VOICE);}catch(Exception e){append("Reconocimiento de voz no disponible en este teléfono.\n");}}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==VOICE&&c==RESULT_OK&&d!=null){ArrayList<String>x=d.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(x!=null&&!x.isEmpty()){message.setText(x.get(0));send();}}else status.setText("CONECTADO");}
    String read(HttpURLConnection c)throws Exception{int code=c.getResponseCode();InputStream in=code<400?c.getInputStream():c.getErrorStream();if(in==null)return "";BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();String l;while((l=r.readLine())!=null)s.append(l);return s.toString();}
    void append(String s){ui(()->{chat.append(s);bottom();});} void bottom(){scroll.post(()->scroll.fullScroll(ScrollView.FOCUS_DOWN));} void ui(Runnable r){runOnUiThread(r);} void fail(String e){ui(()->{status.setText("DESCONECTADO");append("ERROR: "+e+"\n\n");});}
    @Override protected void onResume(){super.onResume();if(baseUrl!=null)health(false);} @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}
