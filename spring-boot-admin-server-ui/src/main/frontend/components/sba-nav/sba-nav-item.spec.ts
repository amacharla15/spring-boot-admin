/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { render, screen } from '@testing-library/vue'
import SbaNavItem from './sba-nav-item.vue'

describe('sba-nav-item click affordance', () => {
  it('applies cursor pointer affordance', () => {
    render(SbaNavItem, {
      global: {
        stubs: {
          RouterLink: { template: '<a class="sba-nav-item"><slot /></a>' }
        }
      },
      slots: {
        default: 'Applications'
      }
    })

    const el = screen.getByText('Applications')
    const clickable = (el.closest('.sba-nav-item') || el) as HTMLElement
    expect(clickable.className).toContain('sba-nav-item')
    expect(clickable.className).toContain('cursor-pointer')
  })
})
