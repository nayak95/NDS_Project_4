import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class VirtualBMap {

    static int[] pBitMap;
    static int m;
    static int[] hashKey;
    static int l;
    static int randomKey;
    static double zeroBits;

    VirtualBMap(int m, int l){
        VirtualBMap.m = m;
        VirtualBMap.l = l;
        pBitMap = new int[m];
        hashKey = new int[l];
        createKeys();
        randomKey = getRandom();
    }

    private static int getRandom(){
        Random rand = new Random();
        return (int)(rand.nextDouble()*Integer.MAX_VALUE);
    }

    public void createKeys(){
        HashSet<Integer> generatedKey = new HashSet<>();
        for(int index = 0; index < l;){
            int key = getRandom();
            if(generatedKey.add(key))
                hashKey[index++] = key;
        }
    }

    public static int getVmapHash(int e) throws NoSuchAlgorithmException {
        MessageDigest mdigest = MessageDigest.getInstance("SHA-256");
        int s = (randomKey ^ e);
        if(mdigest != null)
            s = getDigest(mdigest, s);
        return s % l;
    }

    public static int getBitMapHash(int flowID, int index) throws NoSuchAlgorithmException {
        MessageDigest mdigest = MessageDigest.getInstance("SHA-256");
        int s = hashKey[index] ^ flowID;
        if( mdigest != null)
            s = getDigest(mdigest, s);
        return s % m;
    }

    private static int getDigest(MessageDigest md, int s) {
        byte[] messageDigest = md.digest(String.valueOf(s).getBytes());
        BigInteger big = new BigInteger(1, messageDigest);
        return Math.abs(big.intValue());
    }

    public static void getZeroBits(){
        int count =0, i = 0;
        while(i < m){
            if(pBitMap[i]==1)
                count++;
            i++;
        }
        zeroBits = (m - count)/(m *1.0);
    }

    public static void recordFlow(int flowId, int element) throws NoSuchAlgorithmException {
        int vPos = getVmapHash(element);
        int pPos = getBitMapHash(flowId, vPos);
        pBitMap[pPos] = pBitMap[pPos] | 1;
    }

    public static double queryBitMap(int flowID) throws NoSuchAlgorithmException {
        int count = 0, i = 0;
        while(i < l){
            int hash = getBitMapHash(flowID, i++);
            if(pBitMap[hash] == 1)
                count++;
        }
        return l * (Math.log(zeroBits) - Math.log((l - count)/(l*1.0)));
    }

    public static void implementVirtualBitMap(int m, int l) throws  Exception {
        new VirtualBMap(m, l);
        Map<Integer,Integer> actualCount = new HashMap<>();
        File file = new File("/Users/sanketnayak/Desktop/UF - MSCS/Fall 2020/NDS/Project 4/src/project4input.txt");
        Scanner sc = new Scanner(file);
        int numberOfFlows = sc.nextInt();
        sc.nextLine();
        String[][] input = new String[numberOfFlows][2];
        int i = 0;
        while (sc.hasNextLine() && i < numberOfFlows) {
            input[i] = sc.nextLine().split("\\s+");
            int numberOfElements = Integer.parseInt(String.valueOf(input[i][1]));
            int flowID = input[i][0].hashCode();
            actualCount.put(flowID, numberOfElements);
            for(int j =0; j<numberOfElements; j++){
                int element = getRandom();
                recordFlow(flowID, element);
            }
            i++;
        }
        printCsv(actualCount);
    }

    private static void printCsv(Map<Integer, Integer> actualCount) throws NoSuchAlgorithmException, IOException {
        File outfile = new File("output.csv");
        FileOutputStream outputStream = new FileOutputStream(outfile);
        PrintStream printStream = new PrintStream(outputStream);
        getZeroBits();
        for (Integer key : actualCount.keySet())
            printStream.println(actualCount.get(key) + "," + queryBitMap(key));
        outputStream.close();
        printStream.close();
    }
}
