package day18;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DayEighteenTest {

	private static final String SAMPLE = """
			[[[0,[5,8]],[[1,7],[9,6]]],[[4,[1,2]],[[1,4],2]]]
			[[[5,[2,8]],4],[5,[[9,9],0]]]
			[6,[[[6,2],[5,6]],[[7,6],[4,7]]]]
			[[[6,[0,7]],[0,9]],[4,[9,[9,0]]]]
			[[[7,[6,4]],[3,[1,3]]],[[[5,5],1],9]]
			[[6,[[7,3],[3,2]]],[[[3,8],[5,7]],4]]
			[[[[5,4],[7,7]],8],[[8,3],8]]
			[[9,3],[[9,9],[6,[4,9]]]]
			[[2,[[7,7],7]],[[5,8],[[9,3],[0,2]]]]
			[[[[5,2],5],[8,[3,7]]],[[5,[7,5]],[4,4]]]
			""";

	@Test
	void partOneSample() {
		assertThat(magnitudeOfFinalSum(SAMPLE)).isEqualTo(4140);
	}

	@Test
	void partOneInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(magnitudeOfFinalSum(input)).isEqualTo(4120);
	}

	static long magnitudeOfFinalSum(String input) {
		return Stream.of(input.split("\n"))
				.map(line -> Parser.parseFirstNumber(line, 0))
				.reduce(Number::add)
				.get()
				.magnitude();
	}

	@Test
	void partTwoSample() throws Exception {
		assertThat(largestMagnitudeOfTwoNumbers(SAMPLE)).isEqualTo(3993);
	}

	@Test
	void partTwoInput() throws Exception {
		String input = Files.readString(Paths.get(getClass().getResource("input").toURI()));
		assertThat(largestMagnitudeOfTwoNumbers(input)).isEqualTo(4725);
	}

	private static long largestMagnitudeOfTwoNumbers(String input) {
		return Stream.of(input.split("\n"))
				.mapToLong(a -> Stream.of(input.split("\n"))
						// Parse numbers each time, as Number is mutable
						.mapToLong(b -> Parser.parseFirstNumber(a, 0)
								.add(Parser.parseFirstNumber(b, 0))
								.magnitude())
						.max().getAsLong())
				.max().getAsLong();
	}

}

class Parser {

	static Number parseFirstNumber(String line, int position) {
		char charAt = line.charAt(position);
		if (charAt == '[') {
			Number left = parseFirstNumber(line, position + 1);
			return new Number(left,
					parseFirstNumber(line, position + left.toString().length() + 2));
		}
		return new Number(Character.getNumericValue(charAt)); // Plain value
	}

}

class Number {

	private Number parent;

	private Number left;
	private Number right;

	private Integer value;

	public Number(Number left, Number right) {
		this.left = left;
		this.left.parent = this;
		this.right = right;
		this.right.parent = this;
	}

	public Number(int value) {
		this.value = value;
	}

	public Number add(Number added) {
		Number root = new Number(this, added);
		while (true) {
			List<Number> numbersInOrder = root.stream().toList();
			if (!explode(numbersInOrder) && !split(numbersInOrder)) {
				break;
			}
		}
		return root;
	}

	private static boolean explode(List<Number> numbersInOrder) {
		return numbersInOrder.stream()
				.filter(t -> !t.isRegularNumber()
						&& 5 == Stream.iterate(t, n1 -> n1.parent).takeWhile(Objects::nonNull).count())
				.findFirst()
				.map(exploding -> {
					firstRegularNumber(numbersInOrder, exploding.left, -1)
							.ifPresent(n -> n.value += exploding.left.value);
					firstRegularNumber(numbersInOrder, exploding.right, 1)
							.ifPresent(n -> n.value += exploding.right.value);
					return exploding.parent.replace(exploding, new Number(0));
				})
				.isPresent();
	}

	private static Optional<Number> firstRegularNumber(List<Number> numbersInOrder, Number from, int step) {
		int indexOf = numbersInOrder.indexOf(from);
		while (0 <= (indexOf += step) && indexOf < numbersInOrder.size()) {
			Number leaf = numbersInOrder.get(indexOf);
			if (leaf.isRegularNumber()) {
				return Optional.of(leaf);
			}
		}
		return Optional.empty();
	}

	private static boolean split(List<Number> numbersInOrder) {
		return numbersInOrder.stream()
				.filter(t -> t.isRegularNumber() && 10 <= t.value)
				.findFirst()
				.map(splitting -> splitting.parent.replace(splitting, new Number(
						new Number(splitting.value / 2),
						new Number((splitting.value + 1) / 2))))
				.isPresent();
	}

	private Number replace(Number replaced, Number with) {
		with.parent = this;
		if (left.equals(replaced)) {
			left = with;
		} else if (right.equals(replaced)) {
			right = with;
		} else {
			throw new IllegalStateException("Neither left nor right match " + replaced + " in " + this);
		}
		return with;
	}

	private boolean isRegularNumber() {
		return value != null;
	}

	private Stream<Number> stream() {
		return isRegularNumber()
				? Stream.of(this)
				: Stream.concat(
						left.stream(),
						Stream.concat(
								Stream.of(this),
								right.stream()));
	}

	public long magnitude() {
		return isRegularNumber() ? value : 3 * left.magnitude() + 2 * right.magnitude();
	}

	@Override
	public String toString() {
		return isRegularNumber() ? Long.toString(value) : "[%s,%s]".formatted(left, right);
	}

}
