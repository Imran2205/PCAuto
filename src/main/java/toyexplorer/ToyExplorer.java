/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package toyexplorer;

import java.awt.Desktop;
import java.io.File;

/**
 *
 * @author FridayLab
 */
public class ToyExplorer {

    String dir = "E:\\dev\\Projects\\Encoder_device\\files";
    GUI g;

    ToyExplorer() {
        g = new GUI(this);
        g.setVisible(true);

        File fd = new File(dir);
        File[] files = fd.listFiles();
        System.out.println("total=" + files.length);
        g.setItems(0, files);

//        try{
//        Desktop.getDesktop().open(fd);
//        }catch(Exception ex) {
//            
//        }
    }

    public void selected(int label, String name) {
        System.out.println("selected label=" + label + " name=" + name);
        if (label == 0) {
            File fd = new File(dir + "\\" + name);
            File[] files = fd.listFiles();
            g.setItems(1, files);
        } else if (label == 1) {
            File fd = new File(dir + "\\" + g.parent + "\\" + name);
            System.out.println("fd=" + fd.toString());
            File[] files = fd.listFiles();
            g.setItems(2, files);
        } else if (label == 1) {
            File fd = new File(dir + "\\" + g.parent + "\\" + g.child + "\\" + name);
            System.out.println("file=" + fd.toString());

        }
    }

    public static void main(String[] args) {
        System.out.println("Hi");
        new ToyExplorer();
    }
}
