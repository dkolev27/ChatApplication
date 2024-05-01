package chatapp_combined.Utility;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class SendingMessageUtils {

    public static final int BYTES_FOR_INTEGER = 4; // because an integer is represented as 4 bytes (32 bits)
    public static final int START_IDX = 0;

    public static byte[] convertIntToByteArray(final int i) {
        ByteBuffer buffer = ByteBuffer.allocate(BYTES_FOR_INTEGER);
        buffer.putInt(i);
        return buffer.array();
    }

    public static int convertByteArrayToInt(final byte[] byteArray) {
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        return buffer.getInt();
    }

    public static void sendMessageBytes(String messageToSend, DataOutputStream out) throws IOException {
        byte[] bytesToSend = SendingMessageUtils.bytesToSend(messageToSend);
        out.write(bytesToSend);
        out.flush();
    }

    public static byte[] bytesToSend(String message) {
        final int END_IDX = 2;
        String command = new String(Arrays.copyOfRange(message.getBytes(), START_IDX, END_IDX));

        // get the length of the command in byte[]
        int totalLengthCommand = command.length();
        byte[] totalLengthCommandAsByteArray = convertIntToByteArray(totalLengthCommand);

        // get the command in byte[]
        byte[] commandBytes = command.getBytes();

        // get the length of the message in byte[]
        int totalLengthMessage = message.length() - totalLengthCommand - 1;
        byte[] totalLengthMessageAsByteArray = convertIntToByteArray(totalLengthMessage);

        // get the message in byte[]
        // trim the full message in order to separate it from the command
        final int FROM = totalLengthCommand + 1;
        final int TO = totalLengthMessage + totalLengthCommand + 1;
        String trimmedArr = new String(Arrays.copyOfRange(message.getBytes(), FROM, TO));
        byte[] messageBytes = trimmedArr.getBytes();

        // sum all lengths from the above
        int allBytes =  totalLengthCommandAsByteArray.length + commandBytes.length +
                        totalLengthMessageAsByteArray.length + messageBytes.length;


        return fillAllBytesArray(totalLengthCommandAsByteArray, commandBytes, totalLengthMessageAsByteArray,
                messageBytes, allBytes);
    }

    private static byte[] fillAllBytesArray(byte[] totalLengthCommandAsByteArray, byte[] commandBytes,
                                            byte[] totalLengthMessageAsByteArray, byte[] messageBytes,
                                            int allBytes) {
        // create a new byte[] that will contain all bytes of all byte arrays consequently from the above
        byte[] allBytesArray = new byte[allBytes];

        // fill in the allBytesArray
        int idx = START_IDX;
        for (byte b : totalLengthCommandAsByteArray) {
            allBytesArray[idx++] = b;
        }

        for (byte b : commandBytes) {
            allBytesArray[idx++] = b;
        }

        for (byte b : totalLengthMessageAsByteArray) {
            allBytesArray[idx++] = b;
        }

        for (byte b : messageBytes) {
            allBytesArray[idx++] = b;
        }
        return allBytesArray;
    }
}
