package arrays;

import java.nio.ByteBuffer;

public class SimulateArray {
    public static void main(String[] args) {
        System.out.println("hi");
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.putInt(10);
        buffer.putInt(20);
        buffer.putInt(30);
        buffer.putInt(40);
        buffer.putInt(50);

        String s = buffer.toString();
        System.out.println(s);
    }
}
