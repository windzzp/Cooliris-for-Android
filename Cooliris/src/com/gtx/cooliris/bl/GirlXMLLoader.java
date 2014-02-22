package com.gtx.cooliris.bl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.text.TextUtils;

import com.gtx.cooliris.entity.ImageGroup;
import com.gtx.cooliris.entity.GirlImageSAXHandler;
import com.gtx.cooliris.entity.GirlSAXHandler;

public class GirlXMLLoader {
    // LinkedHashMap<String, Girl> girlsMap = new LinkedHashMap<String, Girl>();

    public static LinkedHashMap<String, ImageGroup> loadGirlsDataFromXML() {
        // 1. Get all girls from "ll_netease_gamegirls" folder
        LinkedHashMap<String, ImageGroup> girlsListMap = loadGirlsListData();

        // 2. Get all girls from "ll_netease_gamegirl_detail" folder
        LinkedHashMap<String, ImageGroup> girlsDetailMap = loadGirlsDetailData();

        // 3. Merge girl data
        mergeGirlsData(girlsListMap, girlsDetailMap);

        return girlsListMap;
    }

    private static LinkedHashMap<String, ImageGroup> loadGirlsListData() {
        File[] girlFiles = null;// getFileList(Environment.GIRLS_ROOTDIR);
        if (null == girlFiles || 0 == girlFiles.length) {
            System.out.println("Girls list XML data invalid!");
            return null;
        }

        LinkedHashMap<String, ImageGroup> girlsListMap = new LinkedHashMap<String, ImageGroup>();

        try {
            SAXParserFactory saxfac = SAXParserFactory.newInstance();
            SAXParser saxParser = saxfac.newSAXParser();
            InputStream is = null;

            // 1. Get all girls from "ll_netease_gamegirls" folder
            ArrayList<ImageGroup> girls = null;

            for (File file : girlFiles) {
                GirlSAXHandler handler = new GirlSAXHandler();
                is = new FileInputStream(file);
                saxParser.parse(is, handler);
                girls = handler.getParsedGirls();
                if (null != girls && girls.size() > 0) {
                    for (ImageGroup girl : girls) {
                        girlsListMap.put(girl.getHomePage(), girl);
                    }
                }

                printGirls(girls);
            }

            System.out.println("Girls count = " + girlsListMap.size());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return girlsListMap;
    }

    private static LinkedHashMap<String, ImageGroup> loadGirlsDetailData() {
        File[] girlDetailFiles = null;// getFileList(Environment.GIRLS_DETAIL_ROOTDIR);
        if (null == girlDetailFiles || 0 == girlDetailFiles.length) {
            System.out.println("Girls detail XML data invalid!");
            return null;
        }

        LinkedHashMap<String, ImageGroup> girlsDetailMap = new LinkedHashMap<String, ImageGroup>();
        try {
            SAXParserFactory saxfac = SAXParserFactory.newInstance();
            SAXParser saxParser = saxfac.newSAXParser();
            InputStream is = null;

            // 2. Get all girls from "ll_netease_gamegirl_detail" folder
            ImageGroup girl = null;
            for (File file : girlDetailFiles) {
                GirlImageSAXHandler imageHandler = new GirlImageSAXHandler();
                is = new FileInputStream(file);
                saxParser.parse(is, imageHandler);
                girl = imageHandler.getParsedGirl();
                if (null != girl) {
                    girlsDetailMap.put(girl.getHomePage(), girl);
                }
            }

            // Print log
            for (Entry<String, ImageGroup> entry : girlsDetailMap.entrySet()) {
                System.out.println(entry.getValue().toString());
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return girlsDetailMap;
    }

    private static void mergeGirlsData(LinkedHashMap<String, ImageGroup> girlsMap, LinkedHashMap<String, ImageGroup> girlsDetailMap) {
        if (null == girlsMap || 0 == girlsMap.size() || null == girlsDetailMap || 0 == girlsDetailMap.size()) {
            System.out.println("Invalid data, Merge Girls Data Failed!");
            return;
        }

        // 3. Merge girl data
        for (Entry<String, ImageGroup> entry : girlsMap.entrySet()) {
            String homePage = entry.getKey();
            ImageGroup girlDest = entry.getValue();
            ImageGroup girlTemp = girlsDetailMap.get(homePage);

            if (null != girlDest && null != girlTemp) {
                girlDest.setTitle(girlTemp.getTitle());
                girlDest.setDate(girlTemp.getDate());
                girlDest.setDescription(girlTemp.getDescription());
                girlDest.setImageList(girlTemp.getImageList());
            } else {
                System.out.println("Do NOT find the girl detail info! title = " + girlDest.getTitle());
            }

            // System.out.println(entry.getValue().toString());
        }
    }

    public static LinkedHashMap<String, ImageGroup> loadGirlsFromXML(String xml) {
        if (TextUtils.isEmpty(xml)) {
            System.out.println("Girls XML data invalid!");
            return null;
        }

        LinkedHashMap<String, ImageGroup> girlsListMap = new LinkedHashMap<String, ImageGroup>();

        try {
            SAXParserFactory saxfac = SAXParserFactory.newInstance();
            SAXParser saxParser = saxfac.newSAXParser();
            ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("uft-8"));

            // 1. Get all girls from xml string
            ArrayList<ImageGroup> girls = null;

            GirlSAXHandler handler = new GirlSAXHandler();
            saxParser.parse(is, handler);
            girls = handler.getParsedGirls();
            if (null != girls && girls.size() > 0) {
                /*
                 * for (Girl girl : girls) { girlsListMap.put(girl.getNameId(), girl); }
                 */
            }

            printGirls(girls);

            System.out.println("Girls count = " + girlsListMap.size());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return girlsListMap;
    }

    public static ArrayList<ImageGroup> loadGirlsFromXML2(String xml) {
        if (TextUtils.isEmpty(xml)) {
            System.out.println("Girls XML data invalid!");
            return null;
        }

        ArrayList<ImageGroup> girls = new ArrayList<ImageGroup>();

        try {
            SAXParserFactory saxfac = SAXParserFactory.newInstance();
            SAXParser saxParser = saxfac.newSAXParser();
            ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));

            // 1. Get all girls from xml string
            GirlSAXHandler handler = new GirlSAXHandler();
            saxParser.parse(is, handler);
            girls = handler.getParsedGirls();

            // printGirls(girls);

            System.out.println("Girls count = " + girls.size());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return girls;
    }

    public static ImageGroup loadGirlImagesFromXML(String imagesXml) {
        if (TextUtils.isEmpty(imagesXml)) {
            System.out.println("Girl Image XML data invalid!");
            return null;
        }

        ImageGroup girl = null;

        try {
            SAXParserFactory saxfac = SAXParserFactory.newInstance();
            SAXParser saxParser = saxfac.newSAXParser();
            ByteArrayInputStream is = new ByteArrayInputStream(imagesXml.getBytes("UTF-8"));

            // 1. Get all girls from xml string
            GirlImageSAXHandler handler = new GirlImageSAXHandler();
            saxParser.parse(is, handler);
            girl = handler.getParsedGirl();

            // printGirls(girls);

            System.out.println("Girls count = " + girl.getImageList().size());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return girl;
    }

    public static File[] getFileList(String rootDir) {
        File girlsRootDir = new File(rootDir);
        if (null != girlsRootDir && girlsRootDir.exists() && girlsRootDir.isDirectory()) {
            File[] girlFiles = girlsRootDir.listFiles();
            if (null != girlFiles) {
                for (int ix = 0; ix < girlFiles.length; ix++) {
                    System.out.println(girlFiles[ix].getAbsolutePath());
                }
            }

            return girlFiles;
        }

        return null;
    }

    private static void printGirls(ArrayList<ImageGroup> girls) {
        if (null != girls) {
            System.out.println("gird_id | home_Page | name | title | description | home_page| thumb_url | thumb_w | thumb_h");
            System.out.println("-------------------------------------------------------------------------------------------");

            for (ImageGroup girl : girls) {
                System.out.println(girl.toString());
            }

            System.out.println("-------------------------------------------------------------------------------------------");
        }
    }
}
