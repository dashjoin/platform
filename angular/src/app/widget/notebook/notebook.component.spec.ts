import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotebookComponent } from './notebook.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('NotebookComponent', () => {
  let component: NotebookComponent;
  let fixture: ComponentFixture<NotebookComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [NotebookComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(NotebookComponent);
    component = fixture.componentInstance;
    component.layout = {} as any
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
