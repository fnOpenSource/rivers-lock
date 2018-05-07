package com.fn.rivers.util;

/**
 * 
 * @author chengwen
 *
 */
public class IpV4Util {
	public static final String DEFAULT_SUBNET_MASK_A = "255.0.0.0";
    public static final String DEFAULT_SUBNET_MASK_B = "255.255.0.0";
    public static final String DEFAULT_SUBNET_MASK_C = "255.255.255.0";
    public static final String TYPE_IP_A = "A";
    public static final String TYPE_IP_B = "B";
    public static final String TYPE_IP_C = "C";
    public static final String TYPE_IP_D = "D";
    public static final String TYPE_IP_LOCATE = "locate";
   
    public static boolean isSameAddress(String resourceIp, String requestIp) throws Exception {
        if (getIpType(resourceIp).equals(getIpType(requestIp))){
            return isSameAddress(resourceIp,requestIp,getIpDefaultMask(getIpType(resourceIp)));
        }
        return false;
    }
 
    private static String getIpDefaultMask(String ipType) throws Exception{
            switch (ipType){
                case TYPE_IP_A:return DEFAULT_SUBNET_MASK_A;
                case TYPE_IP_B:return DEFAULT_SUBNET_MASK_B;
                case TYPE_IP_C:return DEFAULT_SUBNET_MASK_C;
                default:throw new Exception("mask not exists!");
            }
    }

    public static boolean isSameAddress(String resourceIp, String requestIp, String subnetMask) {
        String resourceAddr = getAddrIp(resourceIp, subnetMask);
        String subnetMaskAddr = getAddrIp(requestIp, subnetMask);
        if (resourceAddr.equals(subnetMaskAddr)) {
            return true;
        }
        return false;
    }
 
    private static String getBinaryIp(String data) {
        String[] datas = data.split("\\.");
        String binaryIp = "";
        for (String ipStr : datas) {
            long signIp = Long.parseLong(ipStr);
            String binary = Long.toBinaryString(signIp);
            long binaryInt = Long.parseLong(binary);
            binaryIp += String.format("%08d", binaryInt);
        }
        return binaryIp;
    }
 
    private static String getAddrIp(String ip, String subnetMask) {
        StringBuilder addrIp = new StringBuilder();
        String binaryIp = getBinaryIp(ip);
        String binarySubnetMask = getBinaryIp(subnetMask);
        for (int i = 0; i < 32; i++) {
            byte ipByte = Byte.parseByte(String.valueOf(binaryIp.charAt(i)));
            byte subnetMaskByte = Byte.parseByte(String.valueOf(binarySubnetMask.charAt(i)));
            addrIp.append(ipByte & subnetMaskByte);
        }
        return addrIp.toString();
    }

    public static String getIpType(String ip) throws Exception {
        String binaryIp = getBinaryIp(ip); 
        if (ip.startsWith("127")){
            return TYPE_IP_LOCATE;
        }
        if (binaryIp.startsWith("0")){
            return TYPE_IP_A;
        }
        if (binaryIp.startsWith("10")){
            return TYPE_IP_B;
        }
        if (binaryIp.startsWith("110")){
            return TYPE_IP_C;
        }
        if (binaryIp.startsWith("1110")){
            return TYPE_IP_D;
        }
        throw new Exception("Invalid Ip!");
    }
}
