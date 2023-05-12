import { HttpClientModule } from '@angular/common/http';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { DepInjectorService } from 'src/app/dep-injector.service';
import { MappingComponent } from 'src/app/mapping/mapping.component';

import { SidenavSwitchComponent } from './sidenav-switch.component';

describe('SidenavSwitchComponent', () => {
  let component: SidenavSwitchComponent;
  let fixture: ComponentFixture<SidenavSwitchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MatDialogModule, MatSnackBarModule, HttpClientModule, RouterTestingModule],
      declarations: [SidenavSwitchComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    DepInjectorService.setInjector(TestBed.createComponent(MappingComponent).debugElement.injector);
    fixture = TestBed.createComponent(SidenavSwitchComponent);
    component = fixture.componentInstance;
    component.layout = {} as any
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
