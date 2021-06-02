import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { DepInjectorService } from 'src/app/dep-injector.service';
import { MappingComponent } from 'src/app/mapping/mapping.component';

import { LinksComponent } from './links.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClientModule } from '@angular/common/http';
import { Widget } from '../../instance/widget';

describe('LinksComponent', () => {
  let component: LinksComponent;
  let fixture: ComponentFixture<LinksComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MatDialogModule, MatSnackBarModule, HttpClientModule, RouterTestingModule],
      declarations: [LinksComponent],
      providers: [MatDialog]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    DepInjectorService.setInjector(TestBed.createComponent(MappingComponent).debugElement.injector);
    fixture = TestBed.createComponent(LinksComponent);
    component = fixture.componentInstance;
    component.layout = {} as Widget;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
