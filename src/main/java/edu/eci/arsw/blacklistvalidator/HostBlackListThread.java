package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.LinkedList;

public class HostBlackListThread extends Thread{

    private HostBlacklistsDataSourceFacade skds;
    private int origen, destino;
    private int ocurrencesCount;
    private int checkedListsCount;
    private LinkedList<Integer> blackListOcurrences;
    private String ipaddress;

    public HostBlackListThread (HostBlacklistsDataSourceFacade skds, int source, int target, String ipaddress){
        this.skds = skds;
        this.origen = source;
        this.destino = target;
        this.ipaddress = ipaddress;
        this.blackListOcurrences = new LinkedList<>();
    }

    @Override
    public void run(){
        for (int i=origen; i<destino; i++){
            checkedListsCount ++;
            if (skds.isInBlackListServer(i, ipaddress)){
                blackListOcurrences.add(i);
                ocurrencesCount++;
            }
        }
    }

    public int getOcurrencesCount() {
        return ocurrencesCount;
    }

    public int getCheckedListsCount() {
        return checkedListsCount;
    }

    public LinkedList<Integer> getBlackListOcurrences() {
        return blackListOcurrences;
    }

}
