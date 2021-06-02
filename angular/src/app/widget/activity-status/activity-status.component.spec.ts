import { HttpClientModule } from '@angular/common/http';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { DepInjectorService } from 'src/app/dep-injector.service';
import { MappingComponent } from 'src/app/mapping/mapping.component';

import { ActivityStatusComponent } from './activity-status.component';

describe('ActivityStatusComponent', () => {
  let component: ActivityStatusComponent;
  let fixture: ComponentFixture<ActivityStatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MatDialogModule, MatSnackBarModule, HttpClientModule, RouterTestingModule],
      declarations: [ActivityStatusComponent],
      providers: [MatDialog]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    DepInjectorService.setInjector(TestBed.createComponent(MappingComponent).debugElement.injector);
    fixture = TestBed.createComponent(ActivityStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
