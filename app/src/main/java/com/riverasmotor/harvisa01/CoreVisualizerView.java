package com.riverasmotor.harvisa01;

import android.content.Context;import android.graphics.*;import android.util.AttributeSet;import android.view.View;import java.util.Random;

public class CoreVisualizerView extends View{
 private Paint p=new Paint(3);private float phase=0,level=.18f;private String mode="ONLINE";private Random rnd=new Random();
 public CoreVisualizerView(Context c,AttributeSet a){super(c,a);p.setStrokeCap(Paint.Cap.ROUND);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
 public void setMode(String m){mode=m;invalidate();} public void setLevel(float v){level=Math.max(.08f,Math.min(1f,v));}
 protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight(),cx=w/2,cy=h/2,r=Math.min(w,h)*.30f;phase+=mode.contains("ESCUCH")?.13f:mode.contains("HABLAND")?.18f:.045f;
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.rgb(20,100,145));p.setShadowLayer(14,0,0,Color.rgb(0,150,220));for(int i=0;i<3;i++)c.drawCircle(cx,cy,r+i*18,p);
  p.setShadowLayer(22,0,0,Color.CYAN);p.setStrokeWidth(3);p.setColor(Color.rgb(35,205,255));Path wave=new Path();int n=160;for(int i=0;i<=n;i++){double a=Math.PI*2*i/n;float mod=1f+.15f*(float)Math.sin(a*3+phase)+.08f*(float)Math.sin(a*5-phase*1.4);float rr=r*.72f*mod;float x=cx+(float)Math.cos(a)*rr;float y=cy+(float)Math.sin(a)*rr*(.76f+.10f*(float)Math.sin(a*2+phase));if(i==0)wave.moveTo(x,y);else wave.lineTo(x,y);}wave.close();c.drawPath(wave,p);
  p.setShadowLayer(12,0,0,Color.CYAN);p.setStrokeWidth(5);for(int i=-5;i<=5;i++){float amp=(float)(Math.cos(i*.28)*level);float bh=10+42*amp;float x=cx+i*13;c.drawLine(x,cy+r+54-bh/2,x,cy+r+54+bh/2,p);}postInvalidateDelayed(33);
 }
}