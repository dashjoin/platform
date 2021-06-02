/**
 * event class that is used by components to communicate among each other.
 * The event bubbles up to the root component via (eventChange)="onEvent($event)",
 * at the root node, this.event(event) is called.
 *
 * The event method broadcasts the event to all children by calling event on them.
 *
 * The event method initiates appropriate actions and may decide to stop event propagation.
 * An example would be a reload event which obviously does not require children to be informed
 */
export class DjEvent {

    /**
     * event types:
     *
     * create: a create operation was performed on the backend
     * update: an update operation was performed on the backend
     * delete: an delete operation was performed on the backend
     */
    type: 'create' | 'update' | 'delete' | 'redraw' | 'layout';

    /**
     * layout mode or not?
     */
    busy?: boolean;
}
