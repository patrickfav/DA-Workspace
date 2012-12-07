package at.ac.tuwien.e0426099.simulator.environment.memory.entities;

/**
 * Represents amount of Memory (in Kilobyte in that case)
 * @author PatrickF
 * @since 07.12.12
 */
public class KiB {
	private long amountInKiloByte;

	public KiB(long amountInKiloByte) {
		this.amountInKiloByte = amountInKiloByte;
	}

	public long getAmountInKiloByte() {
		return amountInKiloByte;
	}

	public void setAmountInKiloByte(long amountInKiloByte) {
		this.amountInKiloByte = amountInKiloByte;
	}
}
