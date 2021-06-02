import { HttpClientModule } from '@angular/common/http';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { Expression } from './expression';
import { ExpressionComponent } from './expression.component';

describe('ExpressionComponent', () => {
  let component: ExpressionComponent;
  let fixture: ComponentFixture<ExpressionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ExpressionComponent],
      imports: [HttpClientModule]
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

  it('traverse test', () => {
    expect(Expression.traverse('x', { x: 1 })).toBe(1);
    expect(Expression.traverse('"x"', { x: 1 })).toBe(1);
    expect(Expression.traverse('x.y', { x: { y: 1 } })).toBe(1);
    expect(Expression.traverse('x."y"', { x: { y: 1 } })).toBe(1);
    expect(Expression.traverse('x.y.z', { x: { y: { z: 1 } } })).toBe(1);
    expect(Expression.traverse('x."y".z', { x: { y: { z: 1 } } })).toBe(1);
  });

  it('expression test', () => {
    expect(Expression.template('const', {}, null)).toBe('const');
    expect(Expression.template('${ID}', { ID: 'test' }, null)).toBe('test');
    expect(Expression.template(null, { ID: 'test' }, 'def')).toBe('def');
    expect(Expression.template('${a.b}', { a: { b: 'x' } }, 'def')).toBe('x');
    expect(Expression.template('${a.b other}', { a: { b: 'x' } }, 'def')).toBe('undefined');
    expect(Expression.template('${a.b other}', { a: { 'b other': 'x' } }, 'def')).toBe('x');
    expect(Expression.template('${a."b other"}', { a: { 'b other': 'x' } }, 'def')).toBe('x');
  });
});
