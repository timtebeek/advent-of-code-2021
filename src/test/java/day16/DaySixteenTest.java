package day16;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

class DaySixteenTest {

	private static final String SAMPLE = """
			""";

	@Test
	@Disabled
	void partOneSample() {
		assertThat(something(SAMPLE)).isEqualTo(-1);
	}

	private int something(String sample) {
		// TODO Auto-generated method stub
		return -1;
	}

	@Test
	void parseLiteralTest() {
		String bits = Parser.hexToBits("D2FE28");
		Packet packet = Parser.parseFirstPacket(bits, 0);
		assertThat(packet.startPosition()).isEqualTo(0);
		assertThat(packet.endPosition()).isEqualTo(21);
		assertThat(packet.version()).isEqualTo(6);
		assertThat(packet.type()).isEqualTo(4);
		assertThat(packet.value()).isEqualTo(2021);
	}

	@Test
	void parseOperatorLengthType0Test() {
		String bits = Parser.hexToBits("38006F45291200");
		Packet packet = Parser.parseFirstPacket(bits, 0);
		assertThat(packet.startPosition()).isEqualTo(0);
		assertThat(packet.endPosition()).isEqualTo(6 + 1 + 15 + 27);
		assertThat(packet.version()).isEqualTo(1);
		assertThat(packet.type()).isEqualTo(6);
		assertThat(packet.value()).isEqualTo(-1);
	}

	@Test
	void parseOperatorLengthType1Test() {
		String bits = Parser.hexToBits("EE00D40C823060");
		Packet packet = Parser.parseFirstPacket(bits, 0);
		assertThat(packet.startPosition()).isEqualTo(0);
		assertThat(packet.endPosition()).isEqualTo(6 + 1 + 11 + 3 * 11);
		assertThat(packet.version()).isEqualTo(7);
		assertThat(packet.type()).isEqualTo(3);
		assertThat(packet.value()).isEqualTo(-1);
	}

	@ParameterizedTest
	@CsvSource(textBlock = """
			11010001010,10
			0101001000100100,20
			01010000001,1
			10010000010,2
			00110000011,3
						""")
	void parseSubPackets(String bits, int literalValue) {
		Packet packet = Parser.parseFirstPacket(bits, 0);
		assertThat(packet.value()).isEqualTo(literalValue);
	}

}

class Parser {

	public static int parse(String input) {
		String bits = hexToBits(input);

		int position = 0;

		Packet packet = parseFirstPacket(bits, position);
		return packet.value();
	}

	static String hexToBits(String input) {
		String bits = Stream.of(input.split(""))
				.map(s -> switch (s) {
				case "0" -> "0000";
				case "1" -> "0001";
				case "2" -> "0010";
				case "3" -> "0011";
				case "4" -> "0100";
				case "5" -> "0101";
				case "6" -> "0110";
				case "7" -> "0111";
				case "8" -> "1000";
				case "9" -> "1001";
				case "A" -> "1010";
				case "B" -> "1011";
				case "C" -> "1100";
				case "D" -> "1101";
				case "E" -> "1110";
				case "F" -> "1111";
				default -> throw new IllegalArgumentException("Unexpected value: " + s);
				})
				.collect(joining());
		return bits;
	}

	static Packet parseFirstPacket(String bits, int startPosition) {
		int position = startPosition;
		int packetVersion = Integer.parseInt(bits.substring(position, position + 3), 2);
		int packetType = Integer.parseInt(bits.substring(position + 3, position + 6), 2);
		position += 6;

		// Parse literal
		if (packetType == 4) {
			StringBuilder sb = new StringBuilder();
			while (bits.charAt(position) == '1') {
				String group = bits.substring(position + 1, position + 5);
				sb.append(group);
				position += 5;
			}
			// Last group
			sb.append(bits.substring(position + 1, position + 5));
			position = position + 5;

			// Parse literal value
			int literalValue = Integer.parseInt(sb.toString(), 2);
			return new Packet(startPosition, position, packetVersion, packetType, literalValue, List.of());
		}

		// Parse operator and sub packets
		char lengthTypeId = bits.charAt(position);
		position++;

		List<Packet> subpackets = new ArrayList<>();

		if (lengthTypeId == '0') {
			// the next 15 bits are a number that represents the total length in bits of the sub-packets contained
			int totalLengthInBits = Integer.parseInt(bits.substring(position, position + 15), 2);
			position += 15;

			int endPosition = position + totalLengthInBits;
			while (position < endPosition) {
				Packet subpacket = parseFirstPacket(bits, position);
				subpackets.add(subpacket);
				position = subpacket.endPosition();
			}
		} else {
			// the next 11 bits are a number that represents the number of sub-packets immediately contained
			int numberOfSubPacketsImmediatelyContained = Integer.parseInt(bits.substring(position, position + 11), 2);
			position += 11;

			for (int i = 0; i < numberOfSubPacketsImmediatelyContained; i++) {
				Packet subpacket = parseFirstPacket(bits, position);
				subpackets.add(subpacket);
				position = subpacket.endPosition();
			}
		}

		return new Packet(startPosition, position, packetVersion, packetType, -1, subpackets);
	}

}

record Packet(int startPosition, int endPosition, int version, int type, int value, List<Packet> subpackets) {
}
