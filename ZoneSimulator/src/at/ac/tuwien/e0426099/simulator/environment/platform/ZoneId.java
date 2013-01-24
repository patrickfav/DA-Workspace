package at.ac.tuwien.e0426099.simulator.environment.platform;

import java.util.UUID;

/**
 * @author PatrickF
 * @since 18.01.13
 */
public class ZoneId {
	private UUID id;
	private String name;

	public ZoneId(String name) {
		this.id = UUID.randomUUID();
		this.name = name;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name+"/"+id.toString().substring(0,5);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ZoneId that = (ZoneId) o;

		if (id != null ? !id.equals(that.id) : that.id != null) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}
}
