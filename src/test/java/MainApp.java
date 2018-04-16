import Identify.IdentifyIDCard;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

/**
 * Created by HelloWorld on 2017/8/24.
 */
public class MainApp {
    public static void main(String [] args) throws  Exception{
        BufferedImage bufferedImage = ImageIO.read(new File("F:\\付-E\\File\\ocrtest\\112.jpg"));
        IdentifyIDCard identifyIDCard = new IdentifyIDCard(bufferedImage);
        Map<String,String> Message=identifyIDCard.getIDMessage();
        String name = Message.get("Name");
        String address = Message.get("Address");
        String IDCode = Message.get("IDCode");
        System.out.println("name:" +"\n"+ name +"\n"+ "address:"  +"\n"+ address +"\n"+"idCode:"+"\n"+IDCode);
    }

    @Test
    public void getPath() throws  Exception {
        File directory = new File("");
        System.out.println(directory.getAbsolutePath());
        System.out.println(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "tessdata");
    }
}
