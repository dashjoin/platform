import { HttpClientModule } from '@angular/common/http';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { DepInjectorService } from 'src/app/dep-injector.service';
import { Widget } from 'src/app/instance/widget';
import { MappingComponent } from 'src/app/mapping/mapping.component';

import { DisplayComponent } from './display.component';

describe('DisplayComponent', () => {
  let component: DisplayComponent;
  let fixture: ComponentFixture<DisplayComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MatDialogModule, MatSnackBarModule, HttpClientModule, RouterTestingModule, NoopAnimationsModule],
      declarations: [DisplayComponent],
      providers: [MatDialog]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    DepInjectorService.setInjector(TestBed.createComponent(MappingComponent).debugElement.injector);
    fixture = TestBed.createComponent(DisplayComponent);
    component = fixture.componentInstance;
    component.layout = {} as Widget;
    fixture.detectChanges();
  });

  it('test camel case', () => {
    expect(component.camelCase2display('testMe')).toBeTruthy('Test Me');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
