/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv112.project;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import sun.applet.Main;

/**
 *
 * @author veronika
 */
class SoundThread  implements Runnable{
    private static volatile AtomicBoolean running ;
    private static Clip clip = null;

    public Clip getClip(){
        return clip;
    }
    public SoundThread(boolean s){
        running = new AtomicBoolean(s);
    }
        
    public void run(){
        try {
            clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(
            Main.class.getResourceAsStream("/resources/SleepingBeautyDiamond.wav"));
            clip.open(inputStream);
            clip.start();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(SoundThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(SoundThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SoundThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        while(!running.get()){
            synchronized(running){
                try { 
                    for(int i = 0; i< 10; i++){System.err.println("sound " + i);}
                } catch (Exception e) {
                    terminate();
                    System.err.println("Sound is not working " + e.getMessage() + e.getCause());
                }
            }
            terminate();
            
        }
    }
    
    public AtomicBoolean getRunning(){
        if(isAtTheEnd()){
            running.set(false);
        }
        return running;
    }
    
    public void start() {
        if(!isAtTheEnd()){
            clip.start();
        }else{
            clip.setFramePosition(0);
            clip.start();
        }
        running.set(true);
    }
    
    public boolean isAtTheEnd(){
        return clip.getLongFramePosition() == clip.getFrameLength();
    }
    
    public void terminate() {
        clip.stop();
        running.set(false);
    }
    
}
