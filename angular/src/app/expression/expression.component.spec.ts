import { HttpClientModule } from '@angular/common/http';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { Expression } from './expression';
import { ExpressionComponent } from './expression.component';

describe('ExpressionComponent', () => {
  let component: ExpressionComponent;
  let fixture: ComponentFixture<ExpressionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ExpressionComponent],
      imports: [HttpClientModule, MatDialogModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ExpressionComponent);
    component = fixture.componentInstance;
    component.rootSchema = {};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('expression test', () => {
    expect(Expression.template('const', {}, null)).toBe('const');
    expect(Expression.template('${ID}', { ID: 'test' }, null)).toBe('test');
    expect(Expression.template(null, { ID: 'test' }, 'def')).toBe('def');
    expect(Expression.template('${a.b}', { a: { b: 'x' } }, 'def')).toBe('x');
    expect(Expression.template('${a.b other}', { a: { b: 'x' } }, 'def')).toBe('${a.b other}'); // jsonata syntax error
    expect(Expression.template('${a."b other"}', { a: { 'b other': 'x' } }, 'def')).toBe('x');
    expect(Expression.template('${a.b} - ${a.b}', { a: { b: 'x' } }, 'def')).toBe('x - x');
    expect(Expression.template('${1+2}', { a: { b: 'x' } }, 'def')).toBe('3');
    expect(Expression.template('<%= 1+2 %>', { a: { b: 'x' } }, 'def')).toBe('3');
    expect(Expression.template('<%= a.b %>aa', { a: { b: 'x' } }, 'def')).toBe('xaa');
  });
});
