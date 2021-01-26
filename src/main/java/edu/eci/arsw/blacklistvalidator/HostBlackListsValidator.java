/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    private static AtomicInteger maxBlackList= new AtomicInteger(0);

    private int ocurrencesCount, checkedListsCount;
    private LinkedList<Integer> blackListOcurrences;

    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipaddress suspicious host's IP address.
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipaddress, int threadsNumber){
        
        blackListOcurrences=new LinkedList<>();
        ocurrencesCount=0;
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        checkedListsCount=0;

        int division = 0;

        if (threadsNumber%2 != 0){
            division = (skds.getRegisteredServersCount()- (skds.getRegisteredServersCount()%threadsNumber)) / threadsNumber;
            this.checkHostThreads(threadsNumber, division, skds, ipaddress);
        }else{
            division = skds.getRegisteredServersCount() / threadsNumber;
            this.checkHostThreads(threadsNumber, division, skds, ipaddress);
        }
        
        if (ocurrencesCount>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipaddress);
        }
        else{
            skds.reportAsTrustworthy(ipaddress);
        }                
        
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }
    
    
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());

    /**
     *Método para el uso de multiHilos
     * @param threadsNumber
     * @param division
     * @param sdks
     * @param ipaddress
     */
    public void checkHostThreads(int threadsNumber, int division, HostBlacklistsDataSourceFacade sdks, String ipaddress){
        ArrayList<HostBlackListThread> threads = new ArrayList<>();
        int source = 0;
        int target = division;

        try {
            for (int i = 0; i < threadsNumber; i++) {
                HostBlackListThread thread;
                if (i == (threadsNumber - 1) && (threadsNumber % 2 != 0)) {
                    target += sdks.getRegisteredServersCount() % threadsNumber;
                    thread = new HostBlackListThread(sdks, source, target, ipaddress, maxBlackList, BLACK_LIST_ALARM_COUNT);
                    thread.join();
                } else {
                    thread = new HostBlackListThread(sdks, source, target, ipaddress, maxBlackList, BLACK_LIST_ALARM_COUNT);
                    source = target + 1;
                    target = source + division;
                    thread.join();
                }
                threads.add(thread);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        for (HostBlackListThread thread : threads){
            thread.start();
        }

        for (HostBlackListThread thread : threads){
            try {
                thread.join();
                ocurrencesCount += thread.getOcurrencesCount();
                checkedListsCount += thread.getCheckedListsCount();
                blackListOcurrences.addAll(thread.getBlackListOcurrences());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    
}
