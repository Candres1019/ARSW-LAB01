package edu.eci.arsw.blacklistvalidator;

public class HostBlackListThread extends Thread{

    private String ipAdd;

    public HostBlackListThread(String ip) {
        ipAdd = ip;
    }

    @Override
    public void run(){

    }

}
