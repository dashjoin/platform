import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotebookComponent } from './notebook.component';

describe('NotebookComponent', () => {
  let component: NotebookComponent;
  let fixture: ComponentFixture<NotebookComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NotebookComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(NotebookComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
