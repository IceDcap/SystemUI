/*******************************************************************************
 * Filename:
 * ---------
 *  EmotionGZIPDecompress.java
 *
 * Project:
 * --------
 *com.amigo.emotion
 *
 * Description:
 * ------------
 *  GZIP data decompress
 *
 * Author:
 * -------
 * pengwei@gionee.com
 *
 * Date:
 * 2014.03.05
 ****************************************************************************/

//Gionee <pengwei><2014-03-05> modify for CR01095632 begin
package com.amigo.navi.keyguard.network.connect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.util.Log;

public abstract class GZIPUtils {

    private static final String LOGTAG = "GNGZIPUtils";
    public static final int BUFFER = 1024;
    public static final String EXT = ".gz";

    /**
     * data compress
     * 
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] compress(byte[] data) {
        if (data != null) {
            ByteArrayInputStream bais = null;
            ByteArrayOutputStream baos = null;
            byte[] output = null;
            try {
                bais = new ByteArrayInputStream(data);
                baos = new ByteArrayOutputStream();
                // compress
                compress(bais, baos);
                output = baos.toByteArray();
                baos.flush();
            } catch (Exception e) {
                Log.e(LOGTAG, "compress byte data error:" + e);
            } finally {
                try {
                    if (bais != null) {
                        bais.close();
                    }
                    if (baos != null) {
                        baos.close();
                    }
                } catch (Exception e) {
                    Log.e(LOGTAG, "compress byte data error:" + e);
                }
            }
            return output;
        }
        return null;
    }

    /**
     * data stream compress
     * 
     * @param is
     * @param os
     * @throws Exception
     */
    public static void compress(InputStream is, OutputStream os) {
        if (is != null) {
            GZIPOutputStream gos = null;
            try {
                gos = new GZIPOutputStream(os);

                int count;
                byte data[] = new byte[BUFFER];
                while ((count = is.read(data, 0, BUFFER)) != -1) {
                    gos.write(data, 0, count);
                }

                gos.finish();
                gos.flush();
            } catch (Exception e) {
                Log.e(LOGTAG, "compress stream error:" + e);
            } finally {
                try {
                    if (gos != null) {
                        gos.close();
                    }
                } catch (Exception e) {
                    Log.e(LOGTAG, "compress stream error:" + e);
                }
            }
        }
    }

    /**
     * file compress
     * 
     * @param file
     * @throws Exception
     */
    public static void compress(File file) {
        compress(file, true);
    }

    /**
     * file compress
     * 
     * @param file
     * @param delete
     *            if delete original file
     * @throws Exception
     */
    public static void compress(File file, boolean delete) {
        if (file != null) {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(file);
                fos = new FileOutputStream(file.getPath() + EXT);

                compress(fis, fos);
                fos.flush();

                if (delete) {
                    file.delete();
                }
            } catch (Exception e) {
                Log.e(LOGTAG, "compress file error:" + e);
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    Log.e(LOGTAG, "compress file error:" + e);
                }
            }
        }
    }

    /**
     * file compress
     * 
     * @param path
     * @throws Exception
     */
    public static void compress(String path) {
        compress(path, true);
    }

    /**
     * file compress
     * 
     * @param path
     * @param delete
     *            if delete original file
     * @throws Exception
     */
    public static void compress(String path, boolean delete) {
        File file = new File(path);
        compress(file, delete);
    }

    /**
     * data decompress
     * 
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] decompress(byte[] data) {
        if (data != null) {
            ByteArrayInputStream bais = null;
            ByteArrayOutputStream baos = null;
            try {
                bais = new ByteArrayInputStream(data);
                baos = new ByteArrayOutputStream();

                // decompress
                decompress(bais, baos);
                data = baos.toByteArray();
                baos.flush();
            } catch (Exception e) {
                Log.e(LOGTAG, "decompress byte data error:" + e);
            } finally {
                try {
                    if (baos != null) {
                        baos.close();
                    }
                    if (bais != null) {
                        bais.close();
                    }
                } catch (Exception e) {
                    Log.e(LOGTAG, "decompress byte data error:" + e);
                }
            }
            return data;
        }
        return null;
    }

    /**
     * data stream decompress
     * 
     * @param is
     * @param os
     * @throws Exception
     */
    public static void decompress(InputStream is, OutputStream os) {
        if (is != null) {
            GZIPInputStream gis = null;
            try {
                Log.e(LOGTAG, "decompress begin");
                gis = new GZIPInputStream(is);
                int count;
                byte data[] = new byte[BUFFER];
                while ((count = gis.read(data, 0, BUFFER)) != -1) {
                    os.write(data, 0, count);
                }
                Log.e(LOGTAG, "decompress end");
            } catch (Exception e) {
                Log.e(LOGTAG, "decompress stream error:" + e);
            } finally {
                try {
                    if (gis != null) {
                        gis.close();
                    }
                } catch (Exception e) {
                    Log.e(LOGTAG, "decompress stream error:" + e);
                }
            }
        }
    }

    /**
     * file decompress
     * 
     * @param file
     * @throws Exception
     */
    public static void decompress(File file) {
        decompress(file, true);
    }

    /**
     * file decompress
     * 
     * @param file
     * @param delete
     *            if delete original file
     * @throws Exception
     */
    public static void decompress(File file, boolean delete) {
        if (file != null) {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(file);
                fos = new FileOutputStream(file.getPath().replace(EXT, ""));

                decompress(fis, fos);
                fos.flush();

                if (delete) {
                    file.delete();
                }
            } catch (Exception e) {
                Log.e(LOGTAG, "decompress file error:" + e);
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                } catch (Exception e) {
                    Log.e(LOGTAG, "decompress file error:" + e);
                }
            }
        }
    }

    /**
     * file decompress
     * 
     * @param path
     * @throws Exception
     */
    public static void decompress(String path) {
        decompress(path, true);
    }

    /**
     * file decompress
     * 
     * @param path
     * @param delete
     *            if delete original file
     * @throws Exception
     */
    public static void decompress(String path, boolean delete) {
        File file = new File(path);
        decompress(file, delete);
    }
    
    public static String inputStream2String(InputStream is, String charset) {
        ByteArrayOutputStream baos = null;

        try {
            baos = new ByteArrayOutputStream();
            int i = -1;
            while ((i = is.read()) != -1) {
                baos.write(i);
            }
            return baos.toString(charset);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != baos) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                baos = null;
            }
        }

        return null;
    }
    
}
// Gionee <pengwei><2014-03-05> modify for CR01095632 end
