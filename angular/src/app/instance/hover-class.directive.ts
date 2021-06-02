import { Directive, HostListener, ElementRef, Input } from '@angular/core';

/**
 * appends a CSS class on hover
 */
@Directive({
    selector: '[appHoverClass]'
})
export class HoverClassDirective {

    /**
     * constructor with link to DOM
     */
    constructor(private elementRef: ElementRef) { }

    /**
     * hover class
     */
    @Input('appHoverClass') hoverClass: any;

    /**
     * add css class on enter
     */
    @HostListener('mouseenter') onMouseEnter() {
        this.elementRef.nativeElement.classList.add(this.hoverClass);
    }

    /**
     * remove css class on leave
     */
    @HostListener('mouseleave') onMouseLeave() {
        this.elementRef.nativeElement.classList.remove(this.hoverClass);
    }
}
