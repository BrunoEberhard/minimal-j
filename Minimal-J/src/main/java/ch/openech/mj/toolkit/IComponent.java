package ch.openech.mj.toolkit;

/**
 * 
 * This should be the uber-Interface of Vaadin and Swing components. Of corse thats not possible.
 * So there is a sub Interface IComponentDelegate.<p>
 * 
 * The idea is:
 * <ul>
 * <li>If its a ComponentDelegate, than the getComponent() is called to get the component itself
 * <li>If not, the class itself has to inherit from a component (mostly form some container)
 * </ul>
 * 
 * <h3>warning</h3>
 * Subject to change
 * 
 * @author Bruno
 *
 */
public interface IComponent {


}
