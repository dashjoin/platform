import { HttpClientModule } from '@angular/common/http';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterTestingModule } from '@angular/router/testing';
import { DepInjectorService } from 'src/app/dep-injector.service';
import { MappingComponent } from 'src/app/mapping/mapping.component';

import { TreeComponent } from './tree.component';

describe('TreeComponent', () => {
  let component: TreeComponent;
  let fixture: ComponentFixture<TreeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [MatDialogModule, MatSnackBarModule, HttpClientModule, RouterTestingModule],
      declarations: [TreeComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    DepInjectorService.setInjector(TestBed.createComponent(MappingComponent).debugElement.injector);
    fixture = TestBed.createComponent(TreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
