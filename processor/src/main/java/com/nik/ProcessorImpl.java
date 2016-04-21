package com.nik;

import ru.kr.Parser;
import ru.kr.Sender;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


import org.springframework.beans.factory.annotation.Autowired;


public class ProcessorImpl implements Processor{
    private static final Double MIN = 30D;

    private Properties prop;

//    @Autowired
    private Parser parser;

//    @Autowired
    private Sender sender;

    public void init(){
        prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("config.prop");
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void process()  {
        try {
            Double money = parser.getMoney();
            if (money < MIN){
                List<String> mails = new ArrayList<String>();
                if (!"".equals(prop.getProperty("e-mail1"))){
                    mails.add(prop.getProperty("e-mail1"));
                }
                if (!"".equals(prop.getProperty("e-mail2"))){
                    mails.add(prop.getProperty("e-mail2"));
                }
                if (!"".equals(prop.getProperty("e-mail3"))){
                    mails.add(prop.getProperty("e-mail3"));
                }

                sender.sendOnEMails(mails);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
