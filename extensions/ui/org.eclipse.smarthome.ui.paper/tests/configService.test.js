describe('factory configService', function() {
    var ConfigService;
    beforeEach(function() {
        module('PaperUI');
    });
    beforeEach(inject(function(configService) {
        ConfigService=configService;
     }));  
    it('should require configService', function() {       
        expect(ConfigService).toBeDefined();
    });
    describe('tests for Rendering model', function() {
        var ThingService;
        beforeEach(inject(function($injector, $rootScope,thingService) {
           scope=$rootScope.$new();
           $httpBackend = $injector.get('$httpBackend');
           restConfig = $injector.get('restConfig');
           ThingService=thingService;
        }));     
        it('should accept empty config parameters', function() {
            var params=ConfigService.getRenderingModel();
            expect(params).toEqual([]);
        });
        it('should call item config', function() {
            spyOn(ConfigService,"getItemConfigs").and.callThrough();
            var inputParams=[{type:'none'}];
            var params=ConfigService.getRenderingModel(inputParams);          
            expect(ConfigService.getItemConfigs).toHaveBeenCalled();
        });
        it('should return the default group when no groups', function() {
            var inputParams=[{type:'none'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params.length).toEqual(1);
            expect(params[0].groupName).toEqual("_default");
        });
        it('should return the default group when not found', function() {
            var inputParams=[{type:'text',groupName:'custom'}];
            var groups=[{name:'some'}];
            var params=ConfigService.getRenderingModel(inputParams,groups);
            expect(params[0].groupName).toEqual("_default");
        });
        it('should return the passed group', function() {
            var inputParams=[{type:'text',groupName:'custom'}];
            var groups=[{name:'custom'}];
            var params=ConfigService.getRenderingModel(inputParams,groups);
            expect(params[0].groupName).toEqual("custom");
        });
        it('should return text widget for invalid type param', function() {
            var inputParams=[{type:'none'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("text");
        });
        it('should return dropdown widget for context ITEM', function() {
            var inputParams=[{context:'item'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("select");
        });
        it('should return date widget for context DATE type=Text', function() {
            var inputParams=[{context:'date',type:'text'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("date");
        });
        it('should return date widget for context DATE type!=text', function() {
            var inputParams=[{context:'date',type:''}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
        });
        it('should return dropdown widget for context THING', function() {
            var inputParams=[{context:'thing'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("select");
        });
        it('should call thingService when context=THING', function() {
            var inputParams=[{context:'thing'}];
            $httpBackend.when('GET', restConfig.restPath + "/things").respond([{name:"thing"}]);
            var params=ConfigService.getRenderingModel(inputParams);
            $httpBackend.flush();
            expect(params[0].parameters[0].options.length).toEqual(1);
        });
        it('should return input time widget for context TIME type=text', function() {
            var inputParams=[{context:'time',type:'text'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("time");
        });
        it('should return input time widget for context TIME type!=text', function() {
            var inputParams=[{context:'time',type:''}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toBeUndefined();
        });
        it('should return color widget for context COLOR', function() {
            var inputParams=[{context:'color'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("color");
            expect(params[0].parameters[0].input).toEqual("TEXT");
            expect(params[0].parameters[0].inputType).toEqual("color");
        });
        it('should return script widget for context SCRIPT', function() {
            var inputParams=[{context:'script'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("textarea");
            expect(params[0].parameters[0].inputType).toEqual("text");
            expect(params[0].parameters[0].label).toBeDefined();
        });
        it('should return dayOfWeek widget for context DAYOFWEEK', function() {
            var inputParams=[{context:'dayOfWeek'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("dayofweek");
            expect(params[0].parameters[0].inputType).toEqual("text");
        });
        it('should return password widget for context PASSWORD', function() {
            var inputParams=[{context:'password'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("password");
        });
        it('should return text widget for INVALID context', function() {
            var inputParams=[{context:'none'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("text");
        });
        it('should return dropdown widget for type TEXT with options', function() {
            var inputParams=[{type:'text',options:[1,2]}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("select");
            expect(params[0].parameters[0].options.length).toEqual(2);
        });
        it('should return text widget for type TEXT', function() {
            var inputParams=[{type:'text'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("text");
        });
        it('should return dropdown widget for type INTEGER/DECIMAL with options', function() {
            var inputParams=[{type:'integer',options:[{value:"1"},{value:"2"}]}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("select");
            expect(params[0].parameters[0].options[0].value).toEqual(1);
            inputParams.type='decimal';
            var paramsDecimal=ConfigService.getRenderingModel(inputParams);
            expect(paramsDecimal[0].parameters[0].element).toEqual("select");
            expect(paramsDecimal[0].parameters[0].options[0].value).toEqual(1);
        });
        it('should return text widget for type INTEGER/DECIMAL', function() {
            var inputParams=[{type:'integer'}];
            var params=ConfigService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("number");
            inputParams.type='decimal';
            var paramsDecimal=ConfigService.getRenderingModel(inputParams);
            expect(paramsDecimal[0].parameters[0].element).toEqual("input");
            expect(paramsDecimal[0].parameters[0].inputType).toEqual("number");
        });
    });

});