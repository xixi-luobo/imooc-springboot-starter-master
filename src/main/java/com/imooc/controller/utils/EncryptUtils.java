package com.imooc.controller.utils;

public class EncryptUtils {


    public static byte[] requestKeyByte = new byte[]{(byte) 98, (byte) 65, (byte) 73, (byte) 103, (byte) 118, (byte) 119, (byte) 65, (byte) 117, (byte) 65, (byte) 52, (byte) 116, (byte) 98, (byte) 68, (byte) 114, (byte) 57, (byte) 100};
    public static byte[] responseKeyByte = new byte[]{(byte) 71, (byte) 83, (byte) 55, (byte) 86, (byte) 101, (byte) 108, (byte) 107, (byte) 74, (byte) 108, (byte) 53, (byte) 73, (byte) 84, (byte) 49, (byte) 117, (byte) 119, (byte) 81};


    public static String encryptPostPram(String text) {
        return AESUtils.encrypt(text, requestKeyByte);
    }


    public static String decryptPostPram(String text) {
        return AESUtils.decrypt(text, requestKeyByte);
    }

    public static String encryptBody(String text) {
        return AESUtils.encrypt(text, responseKeyByte);
    }

    public static String decrypt(String text) {
        return AESUtils.decrypt(text, responseKeyByte);
    }


    public static void main(String[] args) {
        System.out.println(encryptBody("F8zv+sJPJ7TJAw5wqpkoOggmMznaEWKVHad+faKHE1sInNz+LKVXIq8erpw9/BSkG7+BlV/C51UPcfE/5NmumK3smoej/Kp9xWRuHTOWweug3hszi7RfY+iqfLemzCbSB6J4Yaa5rWTaHaKqOjZr7QEm8GxHYFwKK6mQ1s+ZTuCy66gkTfAjVMq6qrDOF7iQadE+NYjWI6QSgm/W7/ph5ynDOa/jg676OKnav/3/FzbDyAo/MtQo3ggZXm/mbwF2YW3bmVsSZG6q9S16lDNUKLUo+KxrkkhQ/Bp9Qv6OyUlmup6H1JgVf0SZH8XaXg9Q0p15VakkmXjVl8+LStWkj7X0Yolitn4fNGZHmiuknPrAgNrywPl6qYuSXmqMLZX+d3SpYDIQb+hUsOJiszejmoMv1ZYfoBjPTp+LP/Fiw4qcQXEkdcxMbU24c8641p34Nl3OJfHtkpzplKhOECk6r7QAwuQ851X0Gv2c6s9CuQQgUYXbdHG3UdWRQm3B+PzGh21bF9i6IoIiedovA+ugyLypXj0scasIq+Fy+QErFkd6gFiwG/5A95hxzINYKKmGJdayUl75w3A1KZRSbF7F22dHUAv+U+FjGKKWyAxF2qnS0a6ewpvJh91S47SHj88mAV1sUt8edNIRhVEMN5Kjnl/cM26btq6MRnwdqcIp9MjTDq9DiAPTXT4MX43NkGbJRlukCBFHzFJHe6NjrhBFSmCVimDnASKi8FINDX+8yf1G30K+O2kvvN3hSSCqj2Gy/96KIfwClA8JIi1WB+vZ2IUGVUG4OgARnpjK6jS577ErvMqgT5XzPNEQq1nviTcWdW6I82j0XzsqcfcBFjDDZ00HepqOLDYIqQ+1oNuFUnPoLCgc2tu4+Nial51rY/UpusJgw6CneADabWn4B4TznphxTkqJG9aLhsP9sDjxqxwMt1on2awQmwKmO2GJyrvWKy07CuwivlUEnWKD1cna6GPMZMW4i3Ys4m682NANJQc6H2eIG6P0EouyWD8NJgKEEDDZa9lURfgiYyFq6Y2h1TdnDKRW/wagpxA/o9YNVCYJWv1tbCqJUoQaw1zb7IhsHZP7MMozLHmNLItyIpuX1URzzrproTM9UHAJsYUx16Y84uF9ZtNl8vglyBs33uMiVaZ4qRkGxUN+kF5n5IutAfEzUqg2fKCcUHCHb6r6MmBH9qDMI6+oyo7Y/DDc9F78EywzQxMw7n8QlWXIa7MtKGdRSi/lZqb2BDHZzhsTz6LYCpoqUJ9+fN/NQ8GpKmxBWk+QVTAgfayDomXy3p2K59QhT64TfWNveGGP/McuSqXFED/KcTXJ2mYvuMI2MNtlc02DFMeXST+jEwpukgpjGEMWn8wu17NwIHuUlQVMqWiR8WoB/Z9DJjlRhJ9r61b+Vrcyav6Pvjo9kyWMtMtv6p3+yUH3Mx8CJSqic2u59gzYCjm0VP4vL4gEIUcBRWt0KNCDDBYsaH46TZovJEImYcFrwfWElxG1RaRJun44XoPwI5SLWMUvolnjrQoPR9pyzTMe+1lRg3x6wP3t5EE+c5h5hl354KgiaSbxmGil0NPYQ3xhYuLpV6T/QMtzkMGRydpVFhEW0ZBk5PeQQpTO3LmI7CFS5nWphsFk5vGiTE46I7+UwijhaiwtemhNiibvXm/b56RsK9UaHJztD9hDEQ=="));
    }

}
