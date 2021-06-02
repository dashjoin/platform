import { HttpClientModule } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { DJRuntimeService } from './djruntime.service';

describe('DJRuntimeService', () => {
  let service: DJRuntimeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule, RouterTestingModule]
    });
    service = TestBed.inject(DJRuntimeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
