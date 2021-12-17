package day16;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

class DaySixteenTest {

	@ParameterizedTest
	@CsvSource(textBlock = """
			8A004A801A8002F478,16
			620080001611562C8802118E34,12
			C0015000016115A2E0802F182340,23
			A0016C880162017C3686B18A3D4780,31
			""")
	void partOneSample(String input, int versionSum) {
		assertThat(Parser.parse(input).stream().mapToInt(Packet::version).sum()).isEqualTo(versionSum);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "input")
	void partOneSample(String input) {
		assertThat(Parser.parse(input).stream().mapToInt(Packet::version).sum()).isEqualTo(991);
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
	void parseSubPackets(String bits, int value) {
		Packet packet = Parser.parseFirstPacket(bits, 0);
		assertThat(packet.value()).isEqualTo(value);
	}

	@ParameterizedTest
	@CsvSource(textBlock = """
			C200B40A82,3
			04005AC33890,54
			880086C3E88112,7
			CE00C43D881120,9
			D8005AC2A8F0,1
			F600BC2D8F,0
			9C005AC2F8F0,0
			9C0141080250320F1802104A08,1
			""")
	void evaluate(String bits, int value) {
		assertThat(Parser.parse(bits).eval()).isEqualTo(value);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "input")
	void partTwoSample(String input) {
		assertThat(Parser.parse(input).eval()).isEqualTo(1264485568252L);
	}

}

class Parser {

	public static Packet parse(String input) {
		String bits = hexToBits(input);
		return parseFirstPacket(bits, 0);
	}

	static String hexToBits(String input) {
		return Stream.of(input.split(""))
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
			long literalValue = Long.parseLong(sb.toString(), 2);
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

record Packet(int startPosition, int endPosition, int version, int type, long value, List<Packet> subpackets) {
	Stream<Packet> stream() {
		return Stream.concat(Stream.of(this), subpackets.stream().flatMap(Packet::stream));
	}

	long eval() {
		return switch (type) {
		case 0 -> subpackets.stream().mapToLong(Packet::eval).sum();// sum
		case 1 -> subpackets.size() == 1
				? subpackets.get(0).eval()
				: subpackets.stream().mapToLong(Packet::eval).reduce((a, b) -> a * b).getAsLong(); // product
		case 2 -> subpackets.stream().mapToLong(Packet::eval).min().getAsLong();// minimum
		case 3 -> subpackets.stream().mapToLong(Packet::eval).max().getAsLong();// maximum
		case 4 -> value; // literal value
		case 5 -> subpackets.get(0).eval() > subpackets.get(1).eval() ? 1 : 0; // greater than
		case 6 -> subpackets.get(0).eval() < subpackets.get(1).eval() ? 1 : 0;// less than
		case 7 -> subpackets.get(0).eval() == subpackets.get(1).eval() ? 1 : 0; // equal to
		default -> throw new IllegalArgumentException("Unexpected value: " + type);
		};
	}
}
