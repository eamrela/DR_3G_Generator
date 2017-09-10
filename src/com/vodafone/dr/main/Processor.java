/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vodafone.dr.main;

import com.vodafone.dr.configuration.AppConf;
import com.vodafone.dr.configuration.DR;
import com.vodafone.dr.configuration.DR_Plan;
import com.vodafone.dr.generator.ScriptGenerator;
import com.vodafone.dr.mongo.MongoDB;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class Processor {
    
    private static String workingDir = null;
    private static String scriptsDir = null;
    
    public static void initApp(String confPath){
            System.out.println("Initializing App");
            AppConf.configureApp(confPath);
            System.out.println("Building DR Plan");
            AppConf.configureDR();
            System.out.println("Initializing Mongo DB");
            MongoDB.initializeDB();
            
            workingDir = AppConf.getWorkingDir()+"\\DR_3G_"+AppConf.getMydate();
            scriptsDir = workingDir+"\\scripts";
            new File(scriptsDir).mkdirs();

    }
    
    public static void generateDR(){
        TreeMap<String, DR> Plan = DR_Plan.getDR_PLAN();
        String siteScript = null;
        File mtxDir;
        File targetRNCFile;
        PrintWriter pw;
        for (Map.Entry<String, DR> entry : Plan.entrySet()) {
            siteScript = ScriptGenerator.generateScriptForSite(entry.getValue().getSiteName(),
                                                    entry.getValue().getSourceMTX(),
                                                  entry.getValue().getSourceRNC(), 
                                                  entry.getValue().getTargetMTX(), 
                                                  entry.getValue().getTargetRNC());
            mtxDir = new File(scriptsDir+"/"+entry.getValue().getSourceMTX());
            if(!mtxDir.exists()){
                mtxDir.mkdir();
            }
            targetRNCFile = new File(scriptsDir+"/"+entry.getValue().getSourceMTX()+"/"+entry.getValue().getTargetRNC()+".txt");
        try {
            pw = new PrintWriter(targetRNCFile);
            pw.append(siteScript);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Processor.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
    }
    
    public static void main(String[] args) {
        if(args.length!=1){
            System.out.println("Please set the input paramters");
            System.out.println("Configuration File");
            System.exit(1);
        }
        String conf = args[0];
        initApp(conf);
        generateDR();
        
    }
}
