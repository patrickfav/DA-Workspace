package at.ac.tuwien.e0426099.simulator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author PatrickF
 * @since 06.12.12
 */

@Target(value= {ElementType.TYPE})
@Retention(value= RetentionPolicy.RUNTIME)
public @interface Service {
	String name() default "";

}
