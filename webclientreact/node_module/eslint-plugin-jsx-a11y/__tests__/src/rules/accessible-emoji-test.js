/* eslint-env jest */
/**
 * @fileoverview Enforce <marquee> elements are not used.
 * @author Ethan Cohen
 */

// -----------------------------------------------------------------------------
// Requirements
// -----------------------------------------------------------------------------

import { RuleTester } from 'eslint';
import parserOptionsMapper from '../../__util__/parserOptionsMapper';
import rule from '../../../src/rules/accessible-emoji';

// -----------------------------------------------------------------------------
// Tests
// -----------------------------------------------------------------------------

const ruleTester = new RuleTester();

const expectedError = {
  message: 'Emojis should be wrapped in <span>, have role="img", and have an accessible description with aria-label or aria-labelledby.',
  type: 'JSXOpeningElement',
};

ruleTester.run('accessible-emoji', rule, {
  valid: [
    { code: '<div />;' },
    { code: '<span />' },
    { code: '<span>No emoji here!</span>' },
    { code: '<span role="img" aria-label="Panda face">๐ผ</span>' },
    { code: '<span role="img" aria-label="Snowman">&#9731;</span>' },
    { code: '<span role="img" aria-labelledby="id1">๐ผ</span>' },
    { code: '<span role="img" aria-labelledby="id1">&#9731;</span>' },
    { code: '<span role="img" aria-labelledby="id1" aria-label="Snowman">&#9731;</span>' },
    { code: '<span>{props.emoji}</span>' },
  ].map(parserOptionsMapper),
  invalid: [
    { code: '<span>๐ผ</span>', errors: [expectedError] },
    { code: '<span>foo๐ผbar</span>', errors: [expectedError] },
    { code: '<span>foo ๐ผ bar</span>', errors: [expectedError] },
    { code: '<i role="img" aria-label="Panda face">๐ผ</i>', errors: [expectedError] },
    { code: '<i role="img" aria-labelledby="id1">๐ผ</i>', errors: [expectedError] },
    { code: '<Foo>๐ผ</Foo>', errors: [expectedError] },
  ].map(parserOptionsMapper),
});
