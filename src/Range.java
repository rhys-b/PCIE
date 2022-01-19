/*
*	Author: Rhys B
*	Created: 2021-11-25
*	Modified: 2021-12-09
*
*	Contains information about a range of text in a document.
*/


public class Range {
	private int offset, length;

	public Range() {
		this(-1, -1);
	}

	public Range(int offset, int length) {
		this.offset = offset;
		this.length = length;
	}

	public void setOffset(int i) {
		offset = i;
	}

	public void moveOffset(int i) {
		setLength(getLength() - (i - getOffset()));
		setOffset(i);
	}

	public int getOffset() {
		return offset;
	}

	public void setLength(int i) {
		length = i;
	}

	public int getLength() {
		return length;
	}

	public void setEnd(int i) {
		length = i - offset + 1;
	}

	public int getEnd() {
		return offset + length - 1;
	}

	public void incrementLength() {
		length++;
	}

	public void decrementOffset() {
		length++;
		offset--;
	}

	public void shift(int i) {
		offset += i;
	}

	public void shiftEnd(int amount) {
		length += amount;
	}

	public boolean contains(int i) {
		// Returns true if int i is inside of this range.

		return ((offset <= i) && (getEnd() >= i));
	}

	public boolean containsIgnoreEnd(int i) {
		// Returns true if i is inside of this range,
		// but the last position doesn't count.

		return ((offset <= i) && (getEnd() > i));
	}

	public boolean contains(Range r) {
		// Returns true if the entirety of Range r is inside this range.

		return r.getOffset() >= getOffset() && r.getEnd() <= getEnd();
	}

	public boolean containsExcludeStart(int i) {
		return ((offset < i) && (getEnd() >= i));
	}

	public boolean overlaps(Range r) {
		// Returns true if any part of Range r is in any part of this.

		return	((r.getOffset() >= offset &&
			r.getOffset() <= getEnd()) ||
			(r.getEnd() >= offset &&
			r.getEnd() <= getEnd()));
	}

	@Override
	public String toString() {
		return "Range at " + getOffset() + " to " + getEnd() + " (length " + getLength() + ")";
	}
}
