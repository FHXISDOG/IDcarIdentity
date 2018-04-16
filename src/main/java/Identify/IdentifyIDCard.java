package Identify;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.ImageHelper;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HelloWorld on 2017/8/24.
 */
public class IdentifyIDCard {
    private final double nameX = 570;         //姓名x坐标到照片左下角的x坐标之差
    private final double photoW = 380;         //身份证照片真实宽度
    private final double nameY = 500;         //姓名y坐标到身份证照片左下角y坐标之差
    private final double nameH=100;
    private final double idCodeX=390;   //身份证照片X坐标到照片左下角的X坐标只差
    private final double idCodeY=60;        //身份证照片Y坐标到照片左下角的Y坐标只差
    private final double addressW=580;
    private final double addressH=210;

    private   double Lx;
    private  double Ly;
    private  double Rx;
    private  double Ry;
    private  double imageWidth;
    private  BufferedImage nameImage;
    private  BufferedImage addressImage;
    private  BufferedImage IDCodeImage;

    public IdentifyIDCard(BufferedImage bufferedImage) {
        try {



            //旋转270度
            bufferedImage = ImageHelper.rotateImage(bufferedImage, 270);
            //获取灰度后的图片
            BufferedImage grayImage = ImageHelper.convertImageToGrayscale(bufferedImage);
            //获取二值化的图片
            BufferedImage binaryImage = ImageHelper.convertImageToBinary(grayImage);
            //获取截取后的坐标
            int[] resultCoordinate = removeIDImageBackGround(binaryImage);
            BufferedImage grayResultImage = grayImage.getSubimage(resultCoordinate[0], resultCoordinate[1], resultCoordinate[2], resultCoordinate[3]);
            BufferedImage binaryResultImage = binaryImage.getSubimage(resultCoordinate[0], resultCoordinate[1], resultCoordinate[2], resultCoordinate[3]);
            double[][] coordinate = getIdPhotoCoordinate(binaryResultImage);
            Lx=coordinate[0][0];
            Ly=coordinate[0][1];
            Rx=coordinate[1][0];
            Ry=coordinate[1][1];
            imageWidth= Rx-Lx;
            //用二值化的图片获取背景，用灰度后的图片识别（灰度后的图片识别率更高）。
            int[] nameCoordinate = getNameImageCoordinate();
            nameImage = grayResultImage.getSubimage(nameCoordinate[0], nameCoordinate[1], nameCoordinate[2], nameCoordinate[3]);
            int[] addressCoordinate = getAddressImageCoordinate();
            addressImage = grayResultImage.getSubimage(addressCoordinate[0], addressCoordinate[1], addressCoordinate[2], addressCoordinate[3]);
            //     addressImage=ImageHelper.getScaledInstance(addressImage,addressImage.getWidth()*2,addressImage.getHeight()*2);
            int[] idCoordinate =getIDCodeImageCoordinate(binaryResultImage);
            IDCodeImage = grayResultImage.getSubimage(idCoordinate[0], idCoordinate[1], idCoordinate[2], idCoordinate[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 识别身份证正面信息
     * @return
     */
    public Map<String, String> getIDMessage() {
        Map<String, String> Message = null;
        try {
            ITesseract instance = new Tesseract();
            //将语言设置为中文
            instance.setLanguage("chi_sim");
            String Name = instance.doOCR(nameImage).replaceAll("\n","");
            String Address =instance.doOCR(addressImage).replaceAll("\n","");
            String IDCode =  instance.doOCR(IDCodeImage).replaceAll("\n","");
            Message = new HashMap<String, String>();
            Message.put("Name", Name);
            Message.put("Address", Address);
            Message.put("IDCode", IDCode);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return Message;
    }

    /**
     * 去掉身份证的背景
     * @param bufferedImage
     * @return
     * @throws Exception
     */
    private int[] removeIDImageBackGround(BufferedImage bufferedImage) throws Exception{
        int[] coordinate = new int[4];
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int X=0;
        int Y=0;
        int countWhite = 0;
        int allCountWhite = 0;
        boolean flag = false;
        boolean flag1 =false;
        int count =0;
        int W = 0;
        int H =0;
        //切右边的黑色
        for (int i = width - 1; i > 0; i--) {

            for (int j = height-1; j >0 ; j--) {

                if (bufferedImage.getRGB(i, j) == -1) {
                    if (flag) {
                        W=i;
                        H=j;
                        flag1=true;
                        break;
                    }
                    countWhite++;
                }
            }
            if (countWhite>20) {
                if (Math.abs(countWhite-allCountWhite)>10) {
                    allCountWhite=countWhite;
                    count=0;
                }else {
                    count++;
                    if (count > 20) {
                        flag = true;
                    }
                }
                countWhite=0;
            }
            if (flag1) {
                break;
            }

        }
        bufferedImage = bufferedImage.getSubimage(X, Y, W, H);
        width=bufferedImage.getWidth();
        height=bufferedImage.getHeight();
        W=width;
        H=height;
        //切下边的黑色
        flag = false;
        flag1= false;
        for (int j = height - 1; j > 0; j--) {

            for (int i = 0; i<width ; i++) {

                if (bufferedImage.getRGB(i, j) == -1) {
                    if (flag) {
                        H=j<H?j:H;
                        flag1=true;
                        break;
                    }
                    countWhite++;
                }
            }
            if (countWhite>20) {
                if (Math.abs(countWhite-allCountWhite)>10) {
                    allCountWhite=countWhite;
                    count=0;
                }else {
                    count++;
                    if (count > 20) {
                        flag = true;
                    }
                }
                countWhite=0;
            }
            if (flag1) {
                break;
            }

        }
        bufferedImage = bufferedImage.getSubimage(X, Y, W, H);
        width=bufferedImage.getWidth();
        height=bufferedImage.getHeight();
        W=width;
        H=height;
        //切上边的黑色
        flag = false;
        flag1= false;
        for (int j = 0; j <height; j++) {

            for (int i = 0; i<width ; i++) {

                if (bufferedImage.getRGB(i, j) == -1) {
                    if (flag) {
                        Y=j>Y?j:Y;
                        H=H-j;
                        flag1=true;
                        break;
                    }
                    countWhite++;
                }
            }
            if (countWhite>20) {
                if (Math.abs(countWhite-allCountWhite)>10) {
                    allCountWhite=countWhite;
                    count=0;
                }else {
                    count++;
                    if (count > 20) {
                        flag = true;
                    }
                }
                countWhite=0;
            }
            if (flag1) {
                break;
            }

        }

        // bufferedImage = bufferedImage.getSubimage(X, Y, W, H);
        coordinate[0]=X;
        coordinate[1]=Y;
        coordinate[2]=W;
        coordinate[3]=H;
        return coordinate;
    }

    /**
     * 获取照片左下角和右下角的坐标
     * @param bufferedImage
     * @return
     * @throws Exception
     */
    private double[][] getIdPhotoCoordinate(BufferedImage bufferedImage) throws Exception {
        double [][] coordinate  =new double[2][2];
        int width = bufferedImage.getWidth()-1;         //图片的宽度
        int height = bufferedImage.getHeight()-1;          //图片的长度
        int Rx= 0;          //照片右下角的x坐标
        int Ry= 0;          //照片右下角的y坐标
        int Lx= 0;              //照片左下角的x坐标
        int Ly= 0;              //照片左下角的y坐标
        boolean flag=false;
        //遍历获取照片右下角黑点的坐标
        for(int i=width;i>0;i--) {
            for(int j=height;j>0;j--) {
                int rgb = bufferedImage.getRGB(i, j);
                //如果一个点是黑点，进行判断是不是杂质
                if (rgb !=-1) {
                    flag =true;
                    //如果一个点是黑点就向上再遍历一百个像素点，检查是否全为黑点，如果有白点，则为杂质
                    int x =j;
                    for(;x>j-20;x--) {
                        rgb = bufferedImage.getRGB(i, x);
                        if (rgb == -1) {
                            flag = false;
                            break;      //如果有白点就跳出循环，将标志置为false;
                        }
                    }
                }
                if (flag) {         //如果找到右下角的点，将坐标赋值，跳出内层循环
                    Rx=i;
                    Ry=j;
                    break;
                }
            }
            if (flag) {             //跳出外层循环
                break;
            }
        }

        //获取左下角的坐标

        int Y = Ry-10;          //取到右下角坐标向上取10个像素点，避免照片边缘的干扰
        //假设照片是理想状态，所以直接横向向左遍历像素点
        for(Lx=Rx-10;Lx>0;Lx--) {
            flag=false;
            int rgb = bufferedImage.getRGB(Lx, Y);
            //如果遍历到白点，继续向左遍历100个像素点，如果100个像素点中有黑色，就为中间区域，就从黑点继续向前遍历
            if(rgb==-1){
                flag=true;
                for(int i= Lx;i>Lx-150;i--) {
                    rgb = bufferedImage.getRGB(i, Y);
                    if (rgb !=-1) {             //如果检测到黑点
                        flag=false;
                        Lx=i;
                        break;
                    }
                }
            }
            if (flag) {
                break;
            }
        }
        coordinate[0][0] = Lx;
        coordinate[0][1] = Ry;
        coordinate[1][0]= Rx;
        coordinate[1][1]= Ry;
        return coordinate;
    }

    /**
     * 获取姓名的坐标
     *
     * @return
     * @throws Exception
     */
    private int[] getNameImageCoordinate() throws  Exception {
        int[] nameCoordinate = new int[4];
        int x = 0;  //姓名X坐标
        int y = 0;  //姓名Y坐标
        int w = 0;  //姓名宽度
        int h = 0;  //姓名长度
        w=(int)Math.ceil((nameX/photoW)*imageWidth);
        x=(int)Lx-w;
        y =(int) Ly-(int) Math.ceil((nameY / photoW )*imageWidth);
        h = (int)Math.ceil((nameH/photoW)*imageWidth);
        nameCoordinate[0]=x;
        nameCoordinate[1]=y;
        nameCoordinate[2]=w;
        nameCoordinate[3]=h;
        return nameCoordinate;
    }

    /**
     * 获取地址的坐标
     *
     * @return
     * @throws Exception
     */
     private int[] getAddressImageCoordinate() throws  Exception {
         int[] addressCoordinate = new int[4];
         int x = 0;  //姓名X坐标
         int y = 0;  //姓名Y坐标
         int w = 0;  //姓名宽度
         int h = 0;  //姓名长度
         w = (int)Math.ceil((addressW/photoW)*imageWidth);
         h = (int) Math.ceil((addressH /photoW) * imageWidth);
         x=(int)(Lx-w);
         y = (int) (Ly - h);
         addressCoordinate[0]=x;
         addressCoordinate[1]=y;
         addressCoordinate[2]=w;
         addressCoordinate[3]=h;
         return addressCoordinate;
     }

    /**
     * 获取身份证照片位置坐标
     * @param bufferedImage
     * @return
     * @throws Exception
     */
     private int[] getIDCodeImageCoordinate(BufferedImage bufferedImage) throws Exception {
         int [] idCoordinate = new int[4];
         int x = 0;  //姓名X坐标
         int y = 0;  //姓名Y坐标
         int w = 0;  //姓名宽度
         int h = 0;  //姓名长度
         x= (int)Math.ceil(Lx-(idCodeX/photoW)*imageWidth);
         y=(int)Math.ceil(Ly+(idCodeY/photoW)*imageWidth);
         h=bufferedImage.getHeight()-y;
         w=(int)(Rx-x);
         idCoordinate[0]=x;
         idCoordinate[1]=y;
         idCoordinate[2]=w;
         idCoordinate[3]=h;
         return idCoordinate;
     }
}
