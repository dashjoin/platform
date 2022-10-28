import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { Widget } from 'src/app/instance/widget';

import { HTMLComponent } from './html.component';

describe('HTMLComponent', () => {
  let component: HTMLComponent;
  let fixture: ComponentFixture<HTMLComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [HTMLComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HTMLComponent);
    component = fixture.componentInstance;
    component.layout = {} as Widget;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
