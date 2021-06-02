import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { InstanceComponent } from './instance.component';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DepInjectorService } from '../dep-injector.service';
import { MappingComponent } from '../mapping/mapping.component';

describe('InstanceComponent', () => {
  let component: InstanceComponent;
  let fixture: ComponentFixture<InstanceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [InstanceComponent],
      imports: [MatSnackBarModule, RouterTestingModule, HttpClientModule, MatDialogModule, BrowserAnimationsModule],
      providers: [HttpClient, MatSnackBar, MatDialog]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    DepInjectorService.setInjector(TestBed.createComponent(MappingComponent).debugElement.injector);
    fixture = TestBed.createComponent(InstanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
