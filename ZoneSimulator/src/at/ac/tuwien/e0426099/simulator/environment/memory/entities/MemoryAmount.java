package at.ac.tuwien.e0426099.simulator.environment.memory.entities;

/**
 * Represents amount of Memory
 * @author PatrickF
 * @since 07.12.12
 */
public class MemoryAmount {
	private long amountInKiloByte;

	public MemoryAmount(long amountInKiloByte) {
		this.amountInKiloByte = amountInKiloByte;
	}

	public long getAmountInKiloByte() {
		return amountInKiloByte;
	}

	public void setAmountInKiloByte(long amountInKiloByte) {
		this.amountInKiloByte = amountInKiloByte;
	}

	public long getAmountInByte() {
		return amountInKiloByte *1000;
	}

	public double getAmountInMegaByte() {
		return amountInKiloByte/1000;
	}

	public double getAmountInGigaByte() {
		return amountInKiloByte/1000/1000;
	}
}
