import { HttpClientModule } from '@angular/common/http';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { DepInjectorService } from 'src/app/dep-injector.service';
import { MappingComponent } from 'src/app/mapping/mapping.component';

import { LayoutEditSwitchComponent } from './layout-edit-switch.component';

describe('LayoutEditSwitchComponent', () => {
  let component: LayoutEditSwitchComponent;
  let fixture: ComponentFixture<LayoutEditSwitchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MatDialogModule, MatSnackBarModule, HttpClientModule, RouterTestingModule],
      declarations: [LayoutEditSwitchComponent],
      providers: [MatDialog]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    DepInjectorService.setInjector(TestBed.createComponent(MappingComponent).debugElement.injector);
    fixture = TestBed.createComponent(LayoutEditSwitchComponent);
    component = fixture.componentInstance;
    component.layout = {} as any
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
