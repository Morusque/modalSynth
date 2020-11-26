import processing.core.*; 
import processing.xml.*; 

import controlP5.*; 
import krister.Ess.*; 

import java.applet.*; 
import java.awt.*; 
import java.awt.image.*; 
import java.awt.event.*; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class modalsynth extends PApplet {





ControlP5 controlP5;

AudioFile myFile;
AudioChannel chanL, chanR;

int echant=44100;

Excitator excitator = new Excitator();
Resonator resonator = new Resonator();
Material material = new Material();
Move move = new Move();
Sound sound = new Sound();

class Excitator {
  int type;
  int envelop;
  float[] sample;
  Excitator() {
    this.type=0;
    this.envelop=0;
    this.sample=new float[PApplet.parseInt(random(10000))];
  }
  public void generate() {
    if (type==0) {//rock
      for (int i=0;i<sample.length;i++) {
        if (i==0 || random(200)<1) {
          sample[i]=random(-1,1);
        }
        else{
          sample[i]=sample[i-1]/2;
        }
      }
    }    
    if (type==1) {//marble
      for (int i=0;i<sample.length;i++) {
        sample[i]=cos(i);
      }
    }
    if (type==2) {//dice
      for (int i=0;i<sample.length;i++) {
        if (random(echant)<10 || i==0) {
          sample[i]=1;
        }
        else{
          sample[i]=0;        
        }
      }
    }
    if (envelop==0) {//decay
      for (int i=0;i<sample.length;i++) {
        sample[i]=sample[i]*(sample.length-i)/sample.length;
      }
    }
    if (envelop==1) {//continuous
    }
    if (envelop==2) {//impact
      for (int i=0;i<sample.length;i++) {
        if (i>sample.length/100) {
          sample[i]=0;
        }
      }
    }    
  }
  public float[] getsample() {
    return sample;
  }
  public void setlength(int value) {
    sample=new float[value];  
  }
  public void settype(int value) {
    type=value;
  }  
  public void setenvelop(int value) {
    envelop=value;    
  }
  public void drawWf() {
    int sizx=100;
    int sizy=100;  
    stroke(255);
    fill(0);
    rect(10,140,sizx,sizy);
    for (int i=0;i<sizx;i++) {
      float sMax=0;
      for (int i2=floor(i*sample.length/sizx);i2<floor((i+1)*sample.length/sizx);i2++) {
        if (abs(sample[i2])>abs(sMax)) {
          sMax=sample[i2];
        }
      }
      point(10+i,140+(sizy/2)*(sMax+1));
    }
  }    
}

class Resonator {
  int nbModes;
  int nbContact;
  float[] frequ;
  float[] decay;
  float[][] ampli;
  float[][] stereo;
  float stWidth;
  Resonator() {
    this.nbModes=10;
    this.nbContact=1;
    this.frequ=new float[nbModes];    
    this.decay=new float[nbModes];
    this.ampli=new float[nbModes][nbContact];
    this.stWidth=random(1);
  }
  public void generate(Material mate) {
    int[] harmoNb=new int[nbModes];
    frequ=new float[nbModes];
    decay=new float[nbModes];
    ampli=new float[nbModes][nbContact];
    stereo=new float[nbModes][nbContact];    
    int multipleMax=ceil(22000/mate.getfondamental());
    for (int i=0;i<nbModes;i++) {
      harmoNb[i]=floor(random(random(multipleMax)));
      float detuneEf=harmoNb[i]*mate.getfondamental()*mate.getdetune();
      frequ[i]=harmoNb[i]*mate.getfondamental()+random(-detuneEf,detuneEf);
    }
    for (int i=0;i<nbModes;i++) {
      decay[i]=mate.getrigid()*random(0.9999f,1);
    }
    for (int i=0;i<nbModes;i++) {
      for (int i2=0;i2<nbContact;i2++) {
        ampli[i][i2]=random(1-mate.getcurves(),1+mate.getcurves())*(mate.getp()+(1-mate.getp())*(multipleMax-harmoNb[i])/multipleMax)/(decay[i]+2);
      }
    }
    for (int i=0;i<nbModes;i++) {
      for (int i2=0;i2<nbContact;i2++) {
        stereo[i][i2]=0.5f+random(-stWidth/2,stWidth/2);
      }
    }   
  }
  public int getnbModes() {
    return nbModes;
  }
  public int getnbContact() {
    return nbContact;
  }  
  public float[] getdecay() {
    return decay;
  }
  public float[] getfrequ() {
    return frequ;
  }
  public float[][] getampli() {
    return ampli;
  }
  public float[][] getstereo() {
    return stereo;
  }  
  public void setmodes(int value) {
    nbModes=value;
  }
  public void setcontacts(int value) {
    nbContact=value;
  }
  public void setwidth(float value) {
    stWidth=value;
  }
}

class Material {
  float fondamental;
  float p; 
  float detune;
  float rigid;
  float curves;
  Material() {
    generate();
  }
  public void generate() {
    this.fondamental=random(20,1000);
    this.p=random(1);
    this.detune=random(1);
    this.rigid=map(log(random(0.4f,1)),-1,0,0.999f,1);
    this.curves=random(1);
  }
  public float getfondamental() {
    return fondamental;
  }
  public float getdetune() {
    return detune;
  }
  public float getp() {
    return p;
  }
  public float getrigid() {
    return rigid;
  }
  public float getcurves() {
    return curves;
  }
  public void setfondamental(float value) {
    fondamental=value;
  }
  public void setp(float value) {
    p=value;
  }
  public void setdetune(float value) {
    detune=value;
  }
  public void setrigid(float value) {
    rigid=value;
  }
  public void setcurves(float value) {
    curves=value;
  }  
}

class Move {
  float speed;
  float mvtImprec;
  Move() {
    speed=random(0.0001f);
    mvtImprec=random(0.001f);
  }
  public float getspeed() {
    return speed;
  }
  public float getmvtImprec() {
    return mvtImprec;
  }
}

class Sound {
  float[] sampleL;
  float[] sampleR;
  float place;
  Sound() {
    sampleL=new float[100000];
    sampleR=new float[100000];    
  }
  public void generate(Excitator exci, Resonator reso, Move move) {
    place=0;
    float[] currentGain = new float[reso.getnbModes()];
    for (int i=0;i<currentGain.length;i++) {
      currentGain[i]=0;
    }
    for (int i=0;i<sampleL.length;i++) {
      sampleL[i]=0;
      sampleR[i]=0;
      place=(place+move.getspeed()+random(-move.getmvtImprec(),move.getmvtImprec()))%1;
      for (int modei=0;modei<reso.getnbModes();modei++) {
        if (i<exci.getsample().length) {
          float tauxExc=0;
          for (int contacti=0;contacti<reso.getnbContact();contacti++) {
            float proxim=(1-vrMax(place,contacti/reso.getnbContact(),1))*2/reso.getnbContact();
            tauxExc+=abs(exci.getsample()[i])*proxim*reso.getampli()[modei][contacti];
          }
          currentGain[modei]+=tauxExc;
        }
        currentGain[modei]*=reso.getdecay()[modei];
        float soundToAddL,soundToAddR;
        soundToAddL=soundToAddR=sin((float)i*reso.getfrequ()[modei]*TWO_PI/(float)echant);
        for (int contacti=0;contacti<reso.getnbContact();contacti++) {
          float proxim=1-vrMax(place,contacti/reso.getnbContact(),1)*2/reso.getnbContact();
          soundToAddL*=currentGain[modei]*((reso.getstereo()[modei][contacti])*proxim);
          soundToAddR*=currentGain[modei]*((1-reso.getstereo()[modei][contacti])*proxim);
        }
        soundToAddL*=currentGain[modei];
        soundToAddR*=currentGain[modei];        
        sampleL[i]+=soundToAddL;
        sampleR[i]+=soundToAddR;        
      }
    }
    //normalize
    float valMax=0;
    for (int i=0; i<sampleL.length;i++) {
      valMax=max(valMax,abs(sampleL[i]));
      valMax=max(valMax,abs(sampleR[i]));      
    }
    for (int i=0; i<sampleL.length;i++) {
      sampleL[i]/=valMax;
      sampleR[i]/=valMax;      
    }
  }
  public float[] getsampleL() {
    return sampleL;
  }
  public float[] getsampleR() {
    return sampleR;
  }
}

public void setup() {
  size(500,300);
  Ess.start(this);
  controlP5 = new ControlP5(this);
  initInterface();  
  chanL = new AudioChannel();  
  chanR = new AudioChannel();    
  makeSound();  
}

public void draw() {
  background(32,16,8);
  controlP5.draw();
  excitator.drawWf();    
}

MultiList excType;
controlP5.MultiListButton buttonA;
controlP5.MultiListButton buttonB;

public void initInterface() {
  //Excitator
  ControlGroup exc = controlP5.addGroup("Excitator",10,20);
  Slider excLength = controlP5.addSlider("excLength",1,50000,10000,0,10,50,10);
  excLength.setLabel("Length");
  excLength.setGroup(exc);
  excType = controlP5.addMultiList("excType",0,30,60,20);
  excType.setGroup(exc);
  buttonA = excType.add("excTypeL1",0);
  buttonA.add("excTypeL10",0).setLabel("Rock");
  buttonA.add("excTypeL11",1).setLabel("Marble");
  buttonA.add("excTypeL12",2).setLabel("Dice");
  buttonA.add("excTypeL13",3).setLabel("Brush");
  buttonB = excType.add("excEnveL1",1);
  buttonB.add("excEnveL10",0).setLabel("Decay");
  buttonB.add("excEnveL11",1).setLabel("Continuous");
  buttonB.add("excEnveL12",2).setLabel("Impact");
  //Sound
  ControlGroup sou = controlP5.addGroup("Sound",110,20);
  controlP5.addBang("souGenerate",0,10,20,20);
  controlP5.controller("souGenerate").setLabel("generate");
  controlP5.controller("souGenerate").setGroup(sou);
  //Resonator
  ControlGroup res = controlP5.addGroup("Resonator",210,20);
  Slider resModes = controlP5.addSlider("resModes",1,50,7,0,50,50,10);
  resModes.setLabel("Modes");
  resModes.setGroup(res);
  Slider resContacts = controlP5.addSlider("resContacts",1,10,3,0,70,50,10);
  resContacts.setLabel("Contacts");
  resContacts.setGroup(res);
  Slider resWidth = controlP5.addSlider("resWidth",0,1,0.5f,0,90,50,10);
  resWidth.setLabel("Width");
  resWidth.setGroup(res);
  //Material
  ControlGroup mat = controlP5.addGroup("Material",310,20);
  Slider matFondamental = controlP5.addSlider("matFondamental",20,1000,100,0,50,50,10);
  matFondamental.setLabel("Fontamental");
  matFondamental.setGroup(mat);
  Slider matP = controlP5.addSlider("matP",0,1,0.5f,0,70,50,10);
  matP.setLabel("Frictions");
  matP.setGroup(mat);
  Slider matDetune = controlP5.addSlider("matDetune",0,1,0.5f,0,90,50,10);
  matDetune.setLabel("Detune");
  matDetune.setGroup(mat);
  Slider matRigid = controlP5.addSlider("matRigid",0.4f,1,0.5f,0,110,50,10);
  matRigid.setLabel("Rigid");
  matRigid.setGroup(mat);
  Slider matCurves = controlP5.addSlider("matCurves",0,1,0.5f,0,130,50,10);
  matCurves.setLabel("Curves");
  matCurves.setGroup(mat);
}

public void matCurves(float value) {
  material.setcurves(value);
  resonator.generate(material);
}

public void matRigid(float value) {
  material.setrigid(map(log(value),-1,0,0.999f,1));
  resonator.generate(material);
}

public void matDetune(float value) {
  material.setdetune(value);
  resonator.generate(material);
}

public void matP(float value) {
  material.setp(value);
  resonator.generate(material);
}

public void matFondamental(float value) {
  material.setfondamental(value);
  resonator.generate(material);
}

public void resModes(int value) {
  resonator.setmodes(value);
  resonator.generate(material);
}

public void resContacts(int value) {
  resonator.setcontacts(value);
  resonator.generate(material);
}

public void resWidth(float value) {
  resonator.setwidth(value);
  resonator.generate(material);
}

public void souGenerate() {
  sound.generate(excitator,resonator,move);
  playSound();
}

public void playSound() {
  float[] sL = sound.getsampleL();
  float[] sR = sound.getsampleL();  
  chanL.initChannel(sL.length);
  chanL.samples=sL;
  chanR.initChannel(sR.length);
  chanR.samples=sR;
  chanL.pan(-1);
  chanR.pan(1);
  chanL.play();
  chanR.play();  
}

public void controlEvent(ControlEvent theEvent) {
  if (theEvent.controller().name().length()>=10) {
    if (theEvent.controller().name().substring(0,7).equals("excType")) {
      excitator.settype(PApplet.parseInt(theEvent.value()));
      buttonA.setLabel(theEvent.controller().label());
      excitator.generate();      
    }
    if (theEvent.controller().name().substring(0,7).equals("excEnve")) {
      excitator.setenvelop(PApplet.parseInt(theEvent.value()));
      buttonB.setLabel(theEvent.controller().label());
      excitator.generate();      
    }    
  }
}

public void excLength(int value) {
  excitator.setlength(value);
  excitator.generate();
}

public void makeSound() {
  excitator.generate();
  resonator.generate(material);
  sound.generate(excitator,resonator,move);
  saveFile(sound.getsampleL(),sound.getsampleR());
  saveEss(sound.getsampleL(),sound.getsampleR());
}

public void saveEss(float[] sL, float[] sR) {
  chanL.initChannel(sL.length);
  chanL.samples=sL;
  chanR.initChannel(sR.length);
  chanR.samples=sR;
  myFile=new AudioFile();  
  myFile.open(dataPath("sampleL.wav"),44100,Ess.WRITE);
  myFile.write(chanL);
  myFile.close();
  myFile=new AudioFile();
  myFile.open(dataPath("sampleR.wav"),44100,Ess.WRITE);
  myFile.write(chanR);
  myFile.close();
  chanL.pan(-1);
  chanR.pan(1);
  chanL.play();
  chanR.play();
}

public void saveFile(float[] sL, float[] sR) {
  byte[] bytesL=new byte[sL.length*2];
  byte[] bytesR=new byte[sR.length*2];
  for (int i=0;i<sL.length;i++) {
    int sanL=floor(map(sL[i],-1,1,-32768,32768));
    String expr="";
    if (sanL<0) {
      expr+="1";
    }
    else{
      expr+="0";
    }
    int longbin=binary(abs(sanL)).length();
    for (int i2=0;i2<max(15-longbin,0);i2++) {
      expr+="0";
    }
    expr+=binary(abs(sanL));
    bytesL[i*2+1]=PApplet.parseByte(unbinary(expr.substring(0,8)));
    bytesL[i*2]=PApplet.parseByte(unbinary(expr.substring(8,16)));
    int sanR=floor(map(sR[i],-1,1,-32768,32768));
    expr="";
    if (sanR<0) {
      expr+="1";
    }
    else{
      expr+="0";
    }
    longbin=binary(abs(sanR)).length();
    for (int i2=0;i2<max(15-longbin,0);i2++) {
      expr+="0";
    }
    expr+=binary(abs(sanR));
    bytesR[i*2+1]=PApplet.parseByte(unbinary(expr.substring(0,8)));
    bytesR[i*2]=PApplet.parseByte(unbinary(expr.substring(8,16)));
  }
  byte[] bytesTotal=new byte[bytesL.length*2];  
  for (int i=0;i<bytesL.length*2;i+=4) {
    bytesTotal[i+0]=bytesL[i/2+0];
    bytesTotal[i+1]=bytesL[i/2+1];
    bytesTotal[i+2]=bytesR[i/2+0];
    bytesTotal[i+3]=bytesR[i/2+1];
  }
  saveBytes(dataPath("sample.wav"),bytesTotal);
}

public float vrMax(float a, float b, float m) {
  float d1=b-a;
  if (d1>m/2) {
    d1=d1-m;
  }
  if (d1<-m/2) {
    d1=d1+m;
  }
  return d1;
}

public void mousePressed() {
}

public void stop() {
  Ess.stop();  
  super.stop();
}























































  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#c0c0c0", "modalsynth" });
  }
}
