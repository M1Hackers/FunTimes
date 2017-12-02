import org.apache.commons.codec.binary.Base64

public static void main(String[] args){

    public static String ImportPhoto(File file){
        FileInputStream fileInputStreamReader = new FileInputStream(file);
        byte[] imageData = new byte[(int)file.length()];
        fileInputStreamReader.read(imageData);
        encodedfile = new String(Base64.encodeBase64(bytes),"UTF-8");
        return encodedfile;
    }

    File f = new File("test.png");
    String encodestring = ImportPhoto(f);
    System.out.println(encodestring);
}

