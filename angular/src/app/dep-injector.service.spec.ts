import { TestBed } from '@angular/core/testing';

import { DepInjectorService } from './dep-injector.service';

describe('DepInjectorService', () => {
  let service: DepInjectorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DepInjectorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
