import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { QueryComponent } from './query.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('QueryComponent', () => {
  let component: QueryComponent;
  let fixture: ComponentFixture<QueryComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [QueryComponent],
      imports: [MatDialogModule, BrowserAnimationsModule],
      providers: [MatDialog]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(QueryComponent);
    component = fixture.componentInstance;
    component.value = { ID: '', database: '', query: '' };
    component.schema = { properties: {} };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
